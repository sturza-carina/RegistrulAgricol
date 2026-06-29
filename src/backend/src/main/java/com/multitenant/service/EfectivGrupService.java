package com.multitenant.service;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.model.animal.EfectivGrup;
import com.multitenant.model.persoana.Persoana;
import com.multitenant.model.registru.Gospodarie;
import com.multitenant.repository.EfectivGrupRepository;
import com.multitenant.repository.GospodarieRepository;
import com.multitenant.repository.PersoanaRepository;
import com.multitenant.dto.EfectivGrupDTO;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Serviciu pentru gestionarea efectivelor de animale crescute în grup (păsări, albine etc.).
 *
 * Model snapshot (append-only):
 *   EfectivGrup nu se actualizează în loc — fiecare modificare a numărului de capete
 *   creează un rând nou cu data curentă. Aceasta permite auditarea istoricului complet
 *   al efectivului, conform cerințelor ANSVSA.
 *
 *   Metoda update() creează un snapshot nou, nu modifică rândul existent.
 */
@Service
@Transactional
public class EfectivGrupService {

    private final EfectivGrupRepository efectivGrupRepository;
    private final GospodarieRepository gospodarieRepository;
    private final PersoanaRepository persoanaRepository;
    private final ModelMapper modelMapper;

    public EfectivGrupService(EfectivGrupRepository efectivGrupRepository,
                              GospodarieRepository gospodarieRepository,
                              PersoanaRepository persoanaRepository,
                              ModelMapper modelMapper) {
        this.efectivGrupRepository = efectivGrupRepository;
        this.gospodarieRepository = gospodarieRepository;
        this.persoanaRepository = persoanaRepository;
        this.modelMapper = modelMapper;
    }

    /**
     * Creează un snapshot nou al efectivului.
     * Nu editează rândul existent — fiecare apel creează un rând nou în BD.
     */
    public EfectivGrupDTO create(EfectivGrup grup) {
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals("public")) {
            grup.setTenantId(currentTenant);
        }

        if (grup.getGospodarieId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gospodărie is required");
        }
        if (grup.getProprietarId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Proprietar is required");
        }
        if (grup.getNumarCapeteFamilii() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Numărul de capete/familii trebuie să fie pozitiv");
        }

        Gospodarie gospodarie = gospodarieRepository.findById(grup.getGospodarieId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gospodărie not found"));
        Persoana proprietar = persoanaRepository.findById(grup.getProprietarId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proprietar not found"));

        grup.setGospodarie(gospodarie);
        grup.setProprietar(proprietar);

        // Setăm data înregistrării la azi dacă nu e furnizată explicit (sau e în viitor)
        if (grup.getDataInregistrare() == null) {
            grup.setDataInregistrare(LocalDate.now());
        }

        EfectivGrup saved = efectivGrupRepository.save(grup);
        return modelMapper.map(saved, EfectivGrupDTO.class);
    }

    /**
     * "Update" în modelul snapshot = creează un snapshot nou cu noile valori.
     * Rândul original NU se modifică — rămâne în istoric.
     *
     * @param id ID-ul snapshot-ului de referință (pentru a copia gospodăria/proprietarul dacă nu sunt furnizați)
     * @param updated Datele noului snapshot
     */
    public EfectivGrupDTO addSnapshot(Long id, EfectivGrup updated) {
        // Încărcăm snapshot-ul de referință pentru a moșteni gospodăria/proprietarul dacă lipsesc
        EfectivGrup reference = efectivGrupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group stock not found with id: " + id));

        EfectivGrup snapshot = new EfectivGrup();
        snapshot.setSpecie(updated.getSpecie() != null ? updated.getSpecie() : reference.getSpecie());
        snapshot.setNumarCapeteFamilii(updated.getNumarCapeteFamilii());
        snapshot.setDetalii(updated.getDetalii());
        snapshot.setDataInregistrare(updated.getDataInregistrare() != null
                ? updated.getDataInregistrare() : LocalDate.now());
        snapshot.setTenantId(reference.getTenantId());

        // Gospodărie și proprietar: folosim cel din request dacă sunt furnizați, altfel moștenim
        Long gId = updated.getGospodarieId() != null ? updated.getGospodarieId() : reference.getGospodarie().getId();
        Long pId = updated.getProprietarId() != null ? updated.getProprietarId() : reference.getProprietar().getId();
        snapshot.setGospodarie(gospodarieRepository.findById(gId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gospodărie not found")));
        snapshot.setProprietar(persoanaRepository.findById(pId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proprietar not found")));

        EfectivGrup saved = efectivGrupRepository.save(snapshot);
        return modelMapper.map(saved, EfectivGrupDTO.class);
    }

    @Transactional(readOnly = true)
    public Page<EfectivGrupDTO> getAll(Pageable pageable) {
        return efectivGrupRepository.findAll(pageable)
                .map(entity -> modelMapper.map(entity, EfectivGrupDTO.class));
    }

    @Transactional(readOnly = true)
    public EfectivGrupDTO getById(Long id) {
        EfectivGrup entity = efectivGrupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group stock not found with id: " + id));
        return modelMapper.map(entity, EfectivGrupDTO.class);
    }

    /** Returnează toate snapshot-urile pentru o gospodărie (istoricul complet al efectivului). */
    @Transactional(readOnly = true)
    public Page<EfectivGrupDTO> getHistoryByGospodarieId(Long gospodarieId, Pageable pageable) {
        return efectivGrupRepository.findByGospodarieIdOrderByDataInregistrareDesc(gospodarieId, pageable)
                .map(entity -> modelMapper.map(entity, EfectivGrupDTO.class));
    }

    /** Returnează cel mai recent snapshot per specie pentru o gospodărie (starea curentă). */
    @Transactional(readOnly = true)
    public List<EfectivGrupDTO> getLatestByGospodarieId(Long gospodarieId) {
        return efectivGrupRepository.findLatestSnapshotByGospodarieId(gospodarieId).stream()
                .map(entity -> modelMapper.map(entity, EfectivGrupDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Ștergere soft-proof: ștergerea unui snapshot individual este permisă NUMAI prin ADMIN.
     * NU se permite ștergerea întregului istoric al unui efectiv.
     */
    public void delete(Long id) {
        EfectivGrup grup = efectivGrupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group stock not found with id: " + id));
        efectivGrupRepository.delete(grup);
    }

    @Transactional(readOnly = true)
    public Page<EfectivGrupDTO> getByProprietarId(Long proprietarId, Pageable pageable) {
        return efectivGrupRepository.findByProprietarId(proprietarId, pageable)
                .map(entity -> modelMapper.map(entity, EfectivGrupDTO.class));
    }
}
