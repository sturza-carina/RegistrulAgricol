package com.multitenant.service;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.model.animal.AnimalIndividual;
import com.multitenant.model.persoana.Persoana;
import com.multitenant.model.registru.Gospodarie;
import com.multitenant.repository.AnimalIndividualRepository;
import com.multitenant.repository.GospodarieRepository;
import com.multitenant.repository.PersoanaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@Transactional
public class AnimalIndividualService {

    private final AnimalIndividualRepository animalIndividualRepository;
    private final GospodarieRepository gospodarieRepository;
    private final PersoanaRepository persoanaRepository;

    public AnimalIndividualService(AnimalIndividualRepository animalIndividualRepository,
                                   GospodarieRepository gospodarieRepository,
                                   PersoanaRepository persoanaRepository) {
        this.animalIndividualRepository = animalIndividualRepository;
        this.gospodarieRepository = gospodarieRepository;
        this.persoanaRepository = persoanaRepository;
    }

    public AnimalIndividual create(AnimalIndividual animal) {
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals("public")) {
            animal.setTenantId(currentTenant);
        }

        // Validate and load associations
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

        animal.setGospodarie(gospodarie);
        animal.setProprietar(proprietar);

        return animalIndividualRepository.save(animal);
    }

    @Transactional(readOnly = true)
    public List<AnimalIndividual> getAll() {
        return animalIndividualRepository.findAll();
    }

    @Transactional(readOnly = true)
    public AnimalIndividual getById(Long id) {
        return animalIndividualRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Animal not found with id: " + id));
    }

    public AnimalIndividual update(Long id, AnimalIndividual updated) {
        AnimalIndividual existing = getById(id);

        if (updated.getGospodarieId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gospodărie is required");
        }
        if (updated.getProprietarId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Proprietar is required");
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
        existing.setStareActiva(updated.isStareActiva());

        return animalIndividualRepository.save(existing);
    }

    public void delete(Long id) {
        AnimalIndividual animal = getById(id);
        animalIndividualRepository.delete(animal);
    }

    @Transactional(readOnly = true)
    public List<AnimalIndividual> getByProprietarId(Long proprietarId) {
        return animalIndividualRepository.findByProprietarId(proprietarId);
    }
}
