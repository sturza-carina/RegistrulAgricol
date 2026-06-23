package com.multitenant.service;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.model.animal.AnimalIndividual;
import com.multitenant.model.animal.EvenimentAnimal;
import com.multitenant.model.animal.TipEvenimentAnimal;
import com.multitenant.repository.AnimalIndividualRepository;
import com.multitenant.repository.EvenimentAnimalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
@Transactional
public class EvenimentAnimalService {


    /** Tipuri de evenimente care marchează animalul ca inactiv (terminale). */
    private static final Set<TipEvenimentAnimal> EVENIMENTE_TERMINALE = Set.of(
            TipEvenimentAnimal.VANZARE,
            TipEvenimentAnimal.SACRIFICARE_PROPRIE,
            TipEvenimentAnimal.MOARTE,
            TipEvenimentAnimal.UCIDERE_FOCAR,
            TipEvenimentAnimal.DISPARITIE
    );

    /** Tipurile de origine — un animal poate avea un singur astfel de eveniment. */
    private static final Set<TipEvenimentAnimal> EVENIMENTE_ORIGINE = Set.of(
            TipEvenimentAnimal.NASTERE,
            TipEvenimentAnimal.CUMPARARE,
            TipEvenimentAnimal.TRANSFER_INTRARE
    );

    // Nota: TRATAMENT_VETERINAR nu este nici terminal, nici de origine.
    // Este un eveniment intermediar repetabil (multiple tratamente pe același animal).
    // Nu modifică stareActiva și nu intră în nicio restricție de cardinalitate.


    private final EvenimentAnimalRepository evenimentAnimalRepository;
    private final AnimalIndividualRepository animalIndividualRepository;

    public EvenimentAnimalService(EvenimentAnimalRepository evenimentAnimalRepository,
                                  AnimalIndividualRepository animalIndividualRepository) {
        this.evenimentAnimalRepository = evenimentAnimalRepository;
        this.animalIndividualRepository = animalIndividualRepository;
    }

    /**
     * Adaugă un eveniment nou unui animal existent.
     * Rulează toate validările de stare și cronologice înainte de salvare.
     */
    public EvenimentAnimal adaugaEveniment(Long animalId, EvenimentAnimal eveniment) {
        AnimalIndividual animal = animalIndividualRepository.findById(animalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Animalul cu id=" + animalId + " nu a fost găsit."));

        List<EvenimentAnimal> istoric = evenimentAnimalRepository
                .findByAnimalIdOrderByDataEvenimentDesc(animalId);

        // --- Validări ---
        validateCampuriObligatorii(eveniment);
        validateAnimalActiv(animal);
        validateOrigine(eveniment, istoric);
        validateCronologie(eveniment, istoric);

        // --- Setăm relațiile și meta-datele ---
        eveniment.setAnimal(animal);
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals("public")) {
            eveniment.setTenantId(currentTenant);
        }

        // --- Actualizăm starea animalului dacă e eveniment terminal ---
        if (EVENIMENTE_TERMINALE.contains(eveniment.getTipEveniment())) {
            animal.setStareActiva(false);
            animalIndividualRepository.save(animal);
        }

        return evenimentAnimalRepository.save(eveniment);
    }

    /**
     * Returnează istoricul complet (timeline) al unui animal,
     * ordonat descrescător după dată (cel mai recent primul).
     */
    @Transactional(readOnly = true)
    public List<EvenimentAnimal> getTimeline(Long animalId) {
        if (!animalIndividualRepository.existsById(animalId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Animalul cu id=" + animalId + " nu a fost găsit.");
        }
        return evenimentAnimalRepository.findByAnimalIdOrderByDataEvenimentDesc(animalId);
    }

    // =========================================================================
    // Validări private
    // =========================================================================

    private void validateCampuriObligatorii(EvenimentAnimal eveniment) {
        if (eveniment.getTipEveniment() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Tipul evenimentului este obligatoriu.");
        }
        if (eveniment.getDataEveniment() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Data evenimentului este obligatorie.");
        }
    }

    /**
     * Un animal inactiv (decedat, vândut, sacrificat, dispărut) nu poate primi
     * niciun eveniment suplimentar — istoricul lui este înghețat.
     */
    private void validateAnimalActiv(AnimalIndividual animal) {
        if (!animal.isStareActiva()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Animalul este deja marcat ca inactiv. " +
                    "Nu se pot adăuga noi evenimente unui animal decedat, vândut, sacrificat sau dispărut.");
        }
    }

    /**
     * Un animal poate avea un singur eveniment de origine
     * (Naștere, Cumpărare sau Transfer Intrare).
     */
    private void validateOrigine(EvenimentAnimal evenimentNou, List<EvenimentAnimal> istoric) {
        boolean esteOrigine = EVENIMENTE_ORIGINE.contains(evenimentNou.getTipEveniment());
        if (!esteOrigine) return;

        boolean areDejaOrigine = istoric.stream()
                .anyMatch(e -> EVENIMENTE_ORIGINE.contains(e.getTipEveniment()));

        if (areDejaOrigine) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Animalul are deja un eveniment de origine înregistrat " +
                    "(Naștere, Cumpărare sau Transfer Intrare). " +
                    "Nu se poate adăuga un al doilea eveniment de origine.");
        }
    }

    /**
     * Data noului eveniment nu poate fi anterioară datei ultimului eveniment
     * din istoric (pentru a păstra cronologia corectă în timeline).
     */
    private void validateCronologie(EvenimentAnimal evenimentNou, List<EvenimentAnimal> istoric) {
        if (istoric.isEmpty()) return;

        var dataUltimului = istoric.get(0).getDataEveniment();
        if (evenimentNou.getDataEveniment().isBefore(dataUltimului)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Data evenimentului (" + evenimentNou.getDataEveniment() + ") " +
                    "nu poate fi anterioară ultimului eveniment înregistrat (" + dataUltimului + ").");
        }
    }
}
