package com.multitenant.service;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.model.animal.AnimalIndividual;
import com.multitenant.model.persoana.Persoana;
import com.multitenant.model.registru.Gospodarie;
import com.multitenant.repository.AnimalIndividualRepository;
import com.multitenant.repository.GospodarieRepository;
import com.multitenant.repository.PersoanaRepository;
import com.multitenant.dto.AnimalIndividualDTO;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@Transactional
public class AnimalIndividualService {

    private final AnimalIndividualRepository animalIndividualRepository;
    private final GospodarieRepository gospodarieRepository;
    private final PersoanaRepository persoanaRepository;
    private final CrotalRegistryService crotalRegistryService;
    private final ModelMapper modelMapper;

    public AnimalIndividualService(AnimalIndividualRepository animalIndividualRepository,
                                   GospodarieRepository gospodarieRepository,
                                   PersoanaRepository persoanaRepository,
                                   CrotalRegistryService crotalRegistryService,
                                   ModelMapper modelMapper) {
        this.animalIndividualRepository = animalIndividualRepository;
        this.gospodarieRepository = gospodarieRepository;
        this.persoanaRepository = persoanaRepository;
        this.crotalRegistryService = crotalRegistryService;
        this.modelMapper = modelMapper;
    }

    public AnimalIndividualDTO create(AnimalIndividual animal) {
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals("public")) {
            animal.setTenantId(currentTenant);
        }

        if (animal.getGospodarieId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gospodărie is required");
        }
        if (animal.getProprietarId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Proprietar is required");
        }

        Gospodarie gospodarie = gospodarieRepository.findById(animal.getGospodarieId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gospodărie not found"));
        Persoana proprietar = persoanaRepository.findById(animal.getProprietarId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proprietar not found"));

        crotalRegistryService.validateCrotalGlobalUnic(animal.getNumarCrotal(), null, currentTenant);

        if (animal.getNumarCrotal() != null && !animal.getNumarCrotal().isBlank()
                && animalIndividualRepository.existsByNumarCrotalAndIdNot(animal.getNumarCrotal(), null)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Crotalul '" + animal.getNumarCrotal() + "' este deja înregistrat în această fermă.");
        }

        animal.setGospodarie(gospodarie);
        animal.setProprietar(proprietar);

        animal.setStareActiva(true);

        AnimalIndividual saved = animalIndividualRepository.save(animal);

        crotalRegistryService.rezervaCrotal(saved.getNumarCrotal(), currentTenant, saved.getId());

        return modelMapper.map(saved, AnimalIndividualDTO.class);
    }

    @Transactional(readOnly = true)
    public Page<AnimalIndividualDTO> getAll(Pageable pageable) {
        return animalIndividualRepository.findAll(pageable)
                .map(entity -> modelMapper.map(entity, AnimalIndividualDTO.class));
    }

    @Transactional(readOnly = true)
    public AnimalIndividualDTO getById(Long id) {
        AnimalIndividual entity = animalIndividualRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Animal not found with id: " + id));
        return modelMapper.map(entity, AnimalIndividualDTO.class);
    }

    @Transactional(readOnly = true)
    public Page<AnimalIndividualDTO> getByGospodarieId(Long gospodarieId, Pageable pageable) {
        return animalIndividualRepository.findByGospodarieId(gospodarieId, pageable)
                .map(entity -> modelMapper.map(entity, AnimalIndividualDTO.class));
    }

    public AnimalIndividualDTO update(Long id, AnimalIndividual updated) {
        AnimalIndividual existing = animalIndividualRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Animal not found with id: " + id));
        String currentTenant = TenantContext.getCurrentTenant();

        if (updated.getGospodarieId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gospodărie is required");
        }
        if (updated.getProprietarId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Proprietar is required");
        }

        String crotalNou = updated.getNumarCrotal();
        String crotalVechi = existing.getNumarCrotal();
        boolean crotalSchimbat = crotalNou != null && !crotalNou.equals(crotalVechi);

        if (crotalSchimbat) {
            crotalRegistryService.validateCrotalGlobalUnic(crotalNou, existing.getId(), currentTenant);

            if (animalIndividualRepository.existsByNumarCrotalAndIdNot(crotalNou, existing.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Crotalul '" + crotalNou + "' este deja înregistrat în această fermă.");
            }
        }

        Gospodarie gospodarie = gospodarieRepository.findById(updated.getGospodarieId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gospodărie not found"));
        Persoana proprietar = persoanaRepository.findById(updated.getProprietarId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proprietar not found"));

        existing.setGospodarie(gospodarie);
        existing.setProprietar(proprietar);
        existing.setNumarCrotal(updated.getNumarCrotal());
        existing.setSpecie(updated.getSpecie());
        existing.setRasa(updated.getRasa());
        existing.setSex(updated.getSex());
        existing.setDataNastere(updated.getDataNastere());
        existing.setGreutateKg(updated.getGreutateKg());

        AnimalIndividual saved = animalIndividualRepository.save(existing);

        if (crotalSchimbat) {
            crotalRegistryService.elibereazaCrotal(crotalVechi);
            crotalRegistryService.rezervaCrotal(crotalNou, currentTenant, saved.getId());
        }

        return modelMapper.map(saved, AnimalIndividualDTO.class);
    }

    public void delete(Long id) {
        AnimalIndividual animal = animalIndividualRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Animal not found with id: " + id));
        // Eliberăm crotalul din registrul global la ștergere
        crotalRegistryService.elibereazaCrotal(animal.getNumarCrotal());
        animalIndividualRepository.delete(animal);
    }

    @Transactional(readOnly = true)
    public Page<AnimalIndividualDTO> getByProprietarId(Long proprietarId, Pageable pageable) {
        return animalIndividualRepository.findByProprietarId(proprietarId, pageable)
                .map(entity -> modelMapper.map(entity, AnimalIndividualDTO.class));
    }
}
