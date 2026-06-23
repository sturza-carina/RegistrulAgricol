package com.multitenant.service;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.model.animal.AnimalIndividual;
import com.multitenant.model.animal.EvenimentAnimal;
import com.multitenant.model.animal.TipEvenimentAnimal;
import com.multitenant.model.animal.SpecieAnimal;
import com.multitenant.model.animal.SexAnimal;
import com.multitenant.model.persoana.Persoana;
import com.multitenant.model.registru.Gospodarie;
import com.multitenant.repository.AnimalIndividualRepository;
import com.multitenant.repository.EvenimentAnimalRepository;
import com.multitenant.repository.GospodarieRepository;
import com.multitenant.repository.PersoanaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

/**
 * Serviciu dedicat transferului cross-tenant de animale individuale.
 *
 * Logica de business (SNIIA/ANSVSA):
 *   Când un animal este vândut de un deținător din UAT-A către un deținător din UAT-B:
 *   1. Animalul devine inactiv în schema UAT-A (eveniment VANZARE cu destinatarTenantId)
 *   2. Un animal nou este creat în schema UAT-B, cu același codCrotalie (identitate SNIIA)
 *   3. Întregul istoric al animalului (timeline-ul) este copiat în schema UAT-B
 *   4. Un eveniment TRANSFER_INTRARE este adăugat automat în schema UAT-B
 *   5. Registrul global de crotale (public.crotal_registry) este actualizat
 *
 * Arhitectura multi-tenant și tranzacționalitate:
 *   Deoarece avem schemă separată per tenant (schema routing prin Hibernate),
 *   nu putem folosi o singură tranzacție JPA care să scrie în două scheme simultan.
 *   Pentru ca routing-ul conexiunilor la baze de date (search_path) să funcționeze corect,
 *   fiecare pas trebuie executat în propria tranzacție după setarea TenantContext corespunzător.
 *   Astfel, dezactivăm `@Transactional` la nivel global de metodă și folosim `PlatformTransactionManager`
 *   și `TransactionTemplate` programatic pentru a controla granițele fiecărei tranzacții.
 *
 *   ATENȚIE la rollback/compensare: dacă crearea în schema destinatară eșuează după
 *   ce dezactivarea în sursă a reușit, rulăm o tranzacție de compensare în schema sursă
 *   care reactivează animalul și șterge evenimentul VANZARE.
 */
@Service
public class CrossTenantTransferService {

    private final AnimalIndividualRepository animalRepo;
    private final EvenimentAnimalRepository evenimentRepo;
    private final GospodarieRepository gospodarieRepo;
    private final PersoanaRepository persoanaRepo;
    private final CrotalRegistryService crotalRegistryService;
    private final PlatformTransactionManager transactionManager;

    public CrossTenantTransferService(AnimalIndividualRepository animalRepo,
                                      EvenimentAnimalRepository evenimentRepo,
                                      GospodarieRepository gospodarieRepo,
                                      PersoanaRepository persoanaRepo,
                                      CrotalRegistryService crotalRegistryService,
                                      PlatformTransactionManager transactionManager) {
        this.animalRepo = animalRepo;
        this.evenimentRepo = evenimentRepo;
        this.gospodarieRepo = gospodarieRepo;
        this.persoanaRepo = persoanaRepo;
        this.crotalRegistryService = crotalRegistryService;
        this.transactionManager = transactionManager;
    }

    /**
     * Payload de transfer — conține datele necesare pentru a crea animalul în schema destinatară.
     */
    public record TransferRequest(
            String destinatarTenantId,  // ID-ul tenant-ului destinatar (e.g., "123456")
            Long destinatarGospodarieId, // gospodăria din schema destinatară unde intră animalul
            Long destinatarProprietarId, // proprietarul din schema destinatară
            String detaliiTransfer        // detalii opționale (nr. factură, condiții vânzare etc.)
    ) {}

    /**
     * Transferă un animal individual dintr-un tenant în altul.
     * Nu folosește `@Transactional` la nivel de metodă pentru a asigura conexiuni distincte schema-routed.
     *
     * @param animalId ID-ul local al animalului în schema curentă (sursă)
     * @param request  Datele de transfer
     * @return ID-ul noului animal creat în schema destinatară
     */
    public Long transferAnimal(Long animalId, TransferRequest request) {
        String sourceTenantId = TenantContext.getCurrentTenant();

        if (sourceTenantId == null || sourceTenantId.equals("public")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Transferul nu se poate iniția din contextul public.");
        }

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);

        // === PASUL 1: Validăm datele destinatarului în schema destinatară ÎNAINTE de a modifica sursa ===
        TenantContext.setCurrentTenant(request.destinatarTenantId());
        try {
            txTemplate.execute(status -> {
                gospodarieRepo.findById(request.destinatarGospodarieId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Gospodăria destinatară cu id=" + request.destinatarGospodarieId() +
                                " nu a fost găsită în tenant-ul " + request.destinatarTenantId()));
                persoanaRepo.findById(request.destinatarProprietarId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Proprietarul destinatar cu id=" + request.destinatarProprietarId() +
                                " nu a fost găsit în tenant-ul " + request.destinatarTenantId()));
                return null;
            });
        } finally {
            TenantContext.setCurrentTenant(sourceTenantId);
        }

        // === PASUL 2: Citim animalul și istoricul său din schema sursă ===
        TenantContext.setCurrentTenant(sourceTenantId);
        final AnimalIndividual sursaDto = txTemplate.execute(status -> {
            AnimalIndividual s = animalRepo.findById(animalId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Animalul cu id=" + animalId + " nu a fost găsit."));

            if (!s.isStareActiva()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Animalul este deja inactiv. Nu poate fi transferat.");
            }

            // Inițializăm ID-ul gospodăriei pentru a evita LazyInitializationException
            if (s.getGospodarie() != null) {
                s.getGospodarie().getId();
            }
            return s;
        });

        if (request.destinatarTenantId().equals(sourceTenantId) &&
                sursaDto.getGospodarie() != null &&
                sursaDto.getGospodarie().getId().equals(request.destinatarGospodarieId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Gospodăria destinatară nu poate fi aceeași cu gospodăria sursă.");
        }

        final String sursaCrotal = sursaDto.getNumarCrotal();
        final SpecieAnimal sursaSpecie = sursaDto.getSpecie();
        final String sursaRasa = sursaDto.getRasa();
        final SexAnimal sursaSex = sursaDto.getSex();
        final LocalDate sursaDataNastere = sursaDto.getDataNastere();
        final Double sursaGreutateKg = sursaDto.getGreutateKg();

        List<EvenimentAnimal> istoricSursa = txTemplate.execute(status ->
            evenimentRepo.findByAnimalIdOrderByDataEvenimentDesc(animalId)
        );
        List<EvenimentAnimal> istoricCronologic = istoricSursa.reversed();

        // === PASUL 3: Marcăm animalul ca inactiv în schema sursă (eveniment VANZARE) ===
        txTemplate.execute(status -> {
            TenantContext.setCurrentTenant(sourceTenantId);
            AnimalIndividual s = animalRepo.findById(animalId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Animalul cu id=" + animalId + " nu a fost găsit."));

            EvenimentAnimal evenimentVanzare = new EvenimentAnimal();
            evenimentVanzare.setAnimal(s);
            evenimentVanzare.setTipEveniment(TipEvenimentAnimal.VANZARE);
            evenimentVanzare.setDataEveniment(LocalDate.now());
            evenimentVanzare.setDetalii(request.detaliiTransfer() != null
                    ? request.detaliiTransfer()
                    : "Transfer automat către tenant " + request.destinatarTenantId());
            evenimentVanzare.setDestinatarTenantId(request.destinatarTenantId());
            evenimentVanzare.setTenantId(sourceTenantId);

            evenimentRepo.save(evenimentVanzare);
            s.setStareActiva(false);
            animalRepo.save(s);
            return null;
        });

        // === PASUL 4 & 5 & 6: Creăm animalul în schema destinatară și copiem istoricul ===
        Long newAnimalId;
        try {
            newAnimalId = txTemplate.execute(status -> {
                TenantContext.setCurrentTenant(request.destinatarTenantId());

                Gospodarie destGospodarie = gospodarieRepo.findById(request.destinatarGospodarieId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Gospodăria destinatară nu a fost găsită."));
                Persoana destProprietar = persoanaRepo.findById(request.destinatarProprietarId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Proprietarul destinatar nu a fost găsit."));

                AnimalIndividual destinatar = new AnimalIndividual();
                destinatar.setNumarCrotal(sursaCrotal);
                destinatar.setSpecie(sursaSpecie);
                destinatar.setRasa(sursaRasa);
                destinatar.setSex(sursaSex);
                destinatar.setDataNastere(sursaDataNastere);
                destinatar.setGreutateKg(sursaGreutateKg);
                destinatar.setStareActiva(true);
                destinatar.setGospodarie(destGospodarie);
                destinatar.setProprietar(destProprietar);
                destinatar.setTenantId(request.destinatarTenantId());

                AnimalIndividual savedDest = animalRepo.save(destinatar);

                for (EvenimentAnimal ev : istoricCronologic) {
                    EvenimentAnimal copie = new EvenimentAnimal();
                    copie.setAnimal(savedDest);
                    copie.setTipEveniment(ev.getTipEveniment());
                    copie.setDataEveniment(ev.getDataEveniment());
                    copie.setDetalii(ev.getDetalii());
                    copie.setDestinatarTenantId(ev.getDestinatarTenantId());
                    copie.setTenantId(request.destinatarTenantId());
                    evenimentRepo.save(copie);
                }

                EvenimentAnimal transferIntrare = new EvenimentAnimal();
                transferIntrare.setAnimal(savedDest);
                transferIntrare.setTipEveniment(TipEvenimentAnimal.TRANSFER_INTRARE);
                transferIntrare.setDataEveniment(LocalDate.now());
                transferIntrare.setDetalii("Transfer din tenant " + sourceTenantId +
                        (request.detaliiTransfer() != null ? " — " + request.detaliiTransfer() : ""));
                transferIntrare.setTenantId(request.destinatarTenantId());
                evenimentRepo.save(transferIntrare);

                return savedDest.getId();
            });
        } catch (Exception ex) {
            // COMPENSATION: Reactivăm animalul sursă dacă salvarea în destinație a eșuat
            try {
                txTemplate.execute(status -> {
                    TenantContext.setCurrentTenant(sourceTenantId);
                    AnimalIndividual s = animalRepo.findById(animalId).orElse(null);
                    if (s != null) {
                        s.setStareActiva(true);
                        animalRepo.save(s);
                        List<EvenimentAnimal> evs = evenimentRepo.findByAnimalIdOrderByDataEvenimentDesc(animalId);
                        if (!evs.isEmpty() && evs.get(0).getTipEveniment() == TipEvenimentAnimal.VANZARE) {
                            evenimentRepo.delete(evs.get(0));
                        }
                    }
                    return null;
                });
            } catch (Exception compEx) {
                // ignorăm sau logăm eșecul compensării
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Transferul a eșuat la inserarea în schema destinatară: " + ex.getMessage(), ex);
        } finally {
            TenantContext.setCurrentTenant(sourceTenantId);
        }

        // === PASUL 7: Actualizăm registrul global de crotale (independent de Hibernate tenant context) ===
        crotalRegistryService.transferaCrotal(sursaCrotal, request.destinatarTenantId(), newAnimalId);

        return newAnimalId;
    }
}
