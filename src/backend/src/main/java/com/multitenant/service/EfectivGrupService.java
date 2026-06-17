package com.multitenant.service;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.model.animal.EfectivGrup;
import com.multitenant.model.persoana.Persoana;
import com.multitenant.model.registru.Gospodarie;
import com.multitenant.repository.EfectivGrupRepository;
import com.multitenant.repository.GospodarieRepository;
import com.multitenant.repository.PersoanaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@Transactional
public class EfectivGrupService {

    private final EfectivGrupRepository efectivGrupRepository;
    private final GospodarieRepository gospodarieRepository;
    private final PersoanaRepository persoanaRepository;

    public EfectivGrupService(EfectivGrupRepository efectivGrupRepository,
                              GospodarieRepository gospodarieRepository,
                              PersoanaRepository persoanaRepository) {
        this.efectivGrupRepository = efectivGrupRepository;
        this.gospodarieRepository = gospodarieRepository;
        this.persoanaRepository = persoanaRepository;
    }

    public EfectivGrup create(EfectivGrup grup) {
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals("public")) {
            grup.setTenantId(currentTenant);
        }

        // Validate and load associations
        if (grup.getGospodarieId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gospodărie is required");
        }
        if (grup.getProprietarId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Proprietar is required");
        }

        Gospodarie gospodarie = gospodarieRepository.findById(grup.getGospodarieId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gospodărie not found"));
        Persoana proprietar = persoanaRepository.findById(grup.getProprietarId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proprietar not found"));

        grup.setGospodarie(gospodarie);
        grup.setProprietar(proprietar);

        return efectivGrupRepository.save(grup);
    }

    @Transactional(readOnly = true)
    public List<EfectivGrup> getAll() {
        return efectivGrupRepository.findAll();
    }

    @Transactional(readOnly = true)
    public EfectivGrup getById(Long id) {
        return efectivGrupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group stock not found with id: " + id));
    }

    public EfectivGrup update(Long id, EfectivGrup updated) {
        EfectivGrup existing = getById(id);

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
        existing.setSpecie(updated.getSpecie());
        existing.setNumarCapeteFamilii(updated.getNumarCapeteFamilii());
        existing.setDetalii(updated.getDetalii());

        return efectivGrupRepository.save(existing);
    }

    public void delete(Long id) {
        EfectivGrup grup = getById(id);
        efectivGrupRepository.delete(grup);
    }

    @Transactional(readOnly = true)
    public List<EfectivGrup> getByProprietarId(Long proprietarId) {
        return efectivGrupRepository.findByProprietarId(proprietarId);
    }
}
