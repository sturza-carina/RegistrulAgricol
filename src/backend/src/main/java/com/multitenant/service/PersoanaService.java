package com.multitenant.service;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.model.registru.*;
import com.multitenant.model.persoana.*;
import com.multitenant.repository.PersoanaRepository;
import com.multitenant.repository.GospodarieRepository;
import com.multitenant.dto.PersoanaDTO;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PersoanaService {

    private final PersoanaRepository persoanaRepository;
    private final GospodarieRepository gospodarieRepository;
    private final ModelMapper modelMapper;

    public PersoanaService(PersoanaRepository persoanaRepository,
            GospodarieRepository gospodarieRepository,
            ModelMapper modelMapper) {
        this.persoanaRepository = persoanaRepository;
        this.gospodarieRepository = gospodarieRepository;
        this.modelMapper = modelMapper;
    }

    public PersoanaDTO createPerson(Persoana persoana) {
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals("public")) {
            persoana.setTenantId(currentTenant);
        }

        if (persoana.getGospodarii() != null && !persoana.getGospodarii().isEmpty()) {
            List<Gospodarie> fetchedGospodarii = new ArrayList<>();
            for (Gospodarie g : persoana.getGospodarii()) {
                if (g.getId() != null) {
                    gospodarieRepository.findById(g.getId()).ifPresent(fetchedGospodarii::add);
                }
            }
            persoana.setGospodarii(fetchedGospodarii);
        }

        // Set parent references and validate relations
        if (persoana instanceof PersoanaFizica persoanaFizica) {
            if (persoanaFizica.getIdentityDocuments() != null) {
                for (ActIdentitate doc : persoanaFizica.getIdentityDocuments()) {
                    doc.setPersoana(persoanaFizica);
                    doc.setTenantId(currentTenant);
                }
            }
        }

        if (persoana.getRelations() != null) {
            for (RelatieRudenie relation : persoana.getRelations()) {
                relation.setPersoana(persoana);
                validateAndSetRelatedPerson(relation);
            }
        }

        Persoana saved = persoanaRepository.save(persoana);
        return modelMapper.map(saved, PersoanaDTO.class);
    }

    @Transactional(readOnly = true)
    public Page<PersoanaDTO> getAllPersoaneFizice(Pageable pageable) {
        return persoanaRepository.findByPersonTypeOrderByIdDesc("PHYSICAL_PERSON", pageable)
                .map(entity -> modelMapper.map(entity, PersoanaDTO.class));
    }

    public Page<PersoanaDTO> getAllPersoaneJuridice(Pageable pageable) {
        return persoanaRepository.findByPersonTypeOrderByIdDesc("LEGAL_ENTITY", pageable)
                .map(entity -> modelMapper.map(entity, PersoanaDTO.class));
    }

    @Transactional(readOnly = true)
    public Page<PersoanaDTO> getAllPersons(String search, String type, Pageable pageable) {
        String searchHash = (search != null && !search.trim().isEmpty()) ? com.multitenant.util.CryptoUtils.hashSha256(search.trim()) : null;
        return persoanaRepository.searchPersons(search, searchHash, type, pageable)
                .map(entity -> modelMapper.map(entity, PersoanaDTO.class));
    }

    @Transactional(readOnly = true)
    public Page<PersoanaDTO> getPersonsByGospodarieId(Long gospodarieId, Pageable pageable) {
        return persoanaRepository.findByGospodarieId(gospodarieId, pageable)
                .map(entity -> modelMapper.map(entity, PersoanaDTO.class));
    }

    @Transactional(readOnly = true)
    public PersoanaDTO getPersonById(Long id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID must not be null");
        }
        Persoana entity = persoanaRepository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Persoana not found with id: " + id));
        return modelMapper.map(entity, PersoanaDTO.class);
    }

    public PersoanaDTO updatePerson(Long id, Persoana updatedPerson) {
        Persoana existing = persoanaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Persoana not found"));

        if (!existing.getClass().equals(updatedPerson.getClass())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot change Persoana type");
        }

        String currentTenant = TenantContext.getCurrentTenant();

        // Update common fields
        existing.setAdresa(updatedPerson.getAdresa());
        existing.setPhoneNumber(updatedPerson.getPhoneNumber());
        existing.setEmail(updatedPerson.getEmail());
        existing.setRegisterVolume(updatedPerson.getRegisterVolume());
        existing.setRegisterPosition(updatedPerson.getRegisterPosition());
        existing.setNotes(updatedPerson.getNotes());

        if (updatedPerson.getGospodarii() != null) {
            List<Gospodarie> fetchedGospodarii = new ArrayList<>();
            for (Gospodarie g : updatedPerson.getGospodarii()) {
                if (g.getId() != null) {
                    gospodarieRepository.findById(g.getId()).ifPresent(fetchedGospodarii::add);
                }
            }
            existing.setGospodarii(fetchedGospodarii);
        } else {
            if (existing.getGospodarii() != null) {
                existing.getGospodarii().clear();
            } else {
                existing.setGospodarii(new ArrayList<>());
            }
        }

        // Update type-specific fields
        if (existing instanceof PersoanaFizica existingPhysical
                && updatedPerson instanceof PersoanaFizica updatedPhysical) {
            existingPhysical.setFirstName(updatedPhysical.getFirstName());
            existingPhysical.setLastName(updatedPhysical.getLastName());
            existingPhysical.setCnp(updatedPhysical.getCnp());
            existingPhysical.setDateOfBirth(updatedPhysical.getDateOfBirth());
            existingPhysical.setIsHeadOfHousehold(updatedPhysical.getIsHeadOfHousehold());

            // Merge identity documents using orphanRemoval
            existingPhysical.getIdentityDocuments().clear();
            if (updatedPhysical.getIdentityDocuments() != null) {
                for (ActIdentitate doc : updatedPhysical.getIdentityDocuments()) {
                    doc.setPersoana(existingPhysical);
                    doc.setTenantId(currentTenant);
                    existingPhysical.getIdentityDocuments().add(doc);
                }
            }
        } else if (existing instanceof PersoanaJuridica existingLegal
                && updatedPerson instanceof PersoanaJuridica updatedLegal) {
            existingLegal.setCompanyName(updatedLegal.getCompanyName());
            existingLegal.setCui(updatedLegal.getCui());
            existingLegal.setRegistrationNumber(updatedLegal.getRegistrationNumber());
            existingLegal.setLegalRepresentative(updatedLegal.getLegalRepresentative());
        }

        // Merge relations using orphanRemoval
        existing.getRelations().clear();
        if (updatedPerson.getRelations() != null) {
            for (RelatieRudenie relation : updatedPerson.getRelations()) {
                relation.setPersoana(existing);
                validateAndSetRelatedPerson(relation);
                existing.getRelations().add(relation);
            }
        }

        Persoana saved = persoanaRepository.save(existing);
        return modelMapper.map(saved, PersoanaDTO.class);
    }

    public void deletePerson(Long id) {
        Persoana persoana = persoanaRepository.findById(id)
                .orElse(null);
        if (persoana != null) {
            persoanaRepository.delete(persoana);
        }
    }

    private void validateAndSetRelatedPerson(RelatieRudenie relation) {
        if (relation.getRelatedPerson() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Related Persoana must not be null");
        }
        Long relatedId = relation.getRelatedPerson().getId();
        if (relatedId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Related Persoana must have a valid ID");
        }
        Persoana related = persoanaRepository.findById(relatedId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Related Persoana not found with id: " + relatedId));
        relation.setRelatedPerson(related);
    }

    public void addPersonToGospodarie(Long persoanaId, Long gospodarieId) {
        Persoana persoana = persoanaRepository.findById(persoanaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Persoana not found"));
        Gospodarie gospodarie = gospodarieRepository.findById(gospodarieId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gospodarie not found with id: " + gospodarieId));
        
        if (persoana.getGospodarii() == null) {
            persoana.setGospodarii(new ArrayList<>());
        }
        
        boolean alreadyExists = persoana.getGospodarii().stream().anyMatch(g -> g.getId().equals(gospodarieId));
        if (!alreadyExists) {
            persoana.getGospodarii().add(gospodarie);
            persoanaRepository.save(persoana);
        }
    }
}
