package com.multitenant.service;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.model.registru.*;
import com.multitenant.model.persoana.*;
import com.multitenant.repository.PersoanaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class PersoanaService {

    private final PersoanaRepository persoanaRepository;
    private final com.multitenant.repository.GospodarieRepository gospodarieRepository;

    public PersoanaService(PersoanaRepository persoanaRepository,
            com.multitenant.repository.GospodarieRepository gospodarieRepository) {
        this.persoanaRepository = persoanaRepository;
        this.gospodarieRepository = gospodarieRepository;
    }

    public Persoana createPerson(Persoana persoana) {
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals("public")) {
            persoana.setTenantId(currentTenant);
        }

        if (persoana.getGospodarie() != null && persoana.getGospodarie().getId() != null) {
            long gId = persoana.getGospodarie().getId();
            Gospodarie g = gospodarieRepository.findById(gId).orElse(null);
            persoana.setGospodarie(g);
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

        return persoanaRepository.save(persoana);
    }

    @Transactional(readOnly = true)
    public List<Persoana> getAllPersons(String search, String type) {
        return persoanaRepository.searchPersons(search, type);
    }

    @Transactional(readOnly = true)
    public List<Persoana> getPersonsByGospodarieId(Long gospodarieId) {
        return persoanaRepository.findByGospodarieId(gospodarieId);
    }

    @Transactional(readOnly = true)
    public Persoana getPersonById(Long id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID must not be null");
        }
        return persoanaRepository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Persoana not found with id: " + id));
    }

    public Persoana updatePerson(Long id, Persoana updatedPerson) {
        Persoana existing = getPersonById(id);

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

        if (updatedPerson.getGospodarie() != null && updatedPerson.getGospodarie().getId() != null) {
            long gId = updatedPerson.getGospodarie().getId();
            Gospodarie g = gospodarieRepository.findById(gId).orElse(null);
            existing.setGospodarie(g);
        } else {
            existing.setGospodarie(null);
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

        return persoanaRepository.save(existing);
    }

    public void deletePerson(Long id) {
        Persoana persoana = getPersonById(id);
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
}
