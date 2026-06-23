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
    // Injectat pentru verificarea unicității globale a crotalelor (SNIIA)
    private final CrotalRegistryService crotalRegistryService;

    public AnimalIndividualService(AnimalIndividualRepository animalIndividualRepository,
                                   GospodarieRepository gospodarieRepository,
                                   PersoanaRepository persoanaRepository,
                                   CrotalRegistryService crotalRegistryService) {
        this.animalIndividualRepository = animalIndividualRepository;
        this.gospodarieRepository = gospodarieRepository;
        this.persoanaRepository = persoanaRepository;
        this.crotalRegistryService = crotalRegistryService;
    }

    public AnimalIndividual create(AnimalIndividual animal) {
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

        // Verificăm unicitatea crotalului GLOBAL (cross-tenant) înainte de save.
        // Argumentul excludeId=null semnalează că e un animal nou (fără ID propriu încă).
        crotalRegistryService.validateCrotalGlobalUnic(animal.getNumarCrotal(), null, currentTenant);

        // Verificăm unicitatea și local (în schema tenantului curent) — apărare în adâncime.
        if (animal.getNumarCrotal() != null && !animal.getNumarCrotal().isBlank()
                && animalIndividualRepository.existsByNumarCrotalAndIdNot(animal.getNumarCrotal(), null)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Crotalul '" + animal.getNumarCrotal() + "' este deja înregistrat în această fermă.");
        }

        animal.setGospodarie(gospodarie);
        animal.setProprietar(proprietar);

        // stareActiva se inițializează întotdeauna true la creare.
        // Nu se poate seta direct — se modifică doar prin evenimentele din timeline.
        animal.setStareActiva(true);

        AnimalIndividual saved = animalIndividualRepository.save(animal);

        // Rezervăm crotalul în registrul global DUPĂ salvare (avem ID-ul acum)
        crotalRegistryService.rezervaCrotal(saved.getNumarCrotal(), currentTenant, saved.getId());

        return saved;
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

    @Transactional(readOnly = true)
    public List<AnimalIndividual> getByGospodarieId(Long gospodarieId) {
        return animalIndividualRepository.findByGospodarieId(gospodarieId);
    }

    public AnimalIndividual update(Long id, AnimalIndividual updated) {
        AnimalIndividual existing = getById(id);
        String currentTenant = TenantContext.getCurrentTenant();

        if (updated.getGospodarieId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gospodărie is required");
        }
        if (updated.getProprietarId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Proprietar is required");
        }

        // Dacă crotalul se schimbă, validăm noul crotal global și local
        String crotalNou = updated.getNumarCrotal();
        String crotalVechi = existing.getNumarCrotal();
        boolean crotalSchimbat = crotalNou != null && !crotalNou.equals(crotalVechi);

        if (crotalSchimbat) {
            // Validare globală — excludem animalul curent prin (id, tenantId)
            crotalRegistryService.validateCrotalGlobalUnic(crotalNou, existing.getId(), currentTenant);

            // Validare locală (intra-tenant) — excludem explicit animalul curent
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

        // IMPORTANT: stareActiva NU se copiază din request.
        // Starea se modifică EXCLUSIV prin evenimentele din timeline (EvenimentAnimalService).
        // Dacă un animal este inactiv (vândut/decedat), nu poate deveni activ din nou prin edit.

        AnimalIndividual saved = animalIndividualRepository.save(existing);

        // Actualizăm registrul global dacă crotalul s-a schimbat
        if (crotalSchimbat) {
            crotalRegistryService.elibereazaCrotal(crotalVechi);
            crotalRegistryService.rezervaCrotal(crotalNou, currentTenant, saved.getId());
        }

        return saved;
    }

    public void delete(Long id) {
        AnimalIndividual animal = getById(id);
        // Eliberăm crotalul din registrul global la ștergere
        crotalRegistryService.elibereazaCrotal(animal.getNumarCrotal());
        animalIndividualRepository.delete(animal);
    }

    @Transactional(readOnly = true)
    public List<AnimalIndividual> getByProprietarId(Long proprietarId) {
        return animalIndividualRepository.findByProprietarId(proprietarId);
    }
}
