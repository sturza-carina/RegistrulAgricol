package com.multitenant.service;

import com.multitenant.model.Uat;
import com.multitenant.repository.UatRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UatService {

    private final UatRepository uatRepository;

    public UatService(UatRepository uatRepository) {
        this.uatRepository = uatRepository;
    }

    public Uat createUat(Uat uat) {
        if (uat.getCodSiruta() == null || uat.getCodSiruta().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "codSiruta is required");
        }

        if (uatRepository.existsByCodSiruta(uat.getCodSiruta())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "UAT with codSiruta " + uat.getCodSiruta() + " already exists.");
        }

        if (uat.getIsActive() == null) {
            uat.setIsActive(true);
        }

        return uatRepository.save(uat);
    }

    public List<Uat> getAllUats() {
        return uatRepository.findAll();
    }

    public Uat getUatByCodSiruta(String codSiruta) {
        if (codSiruta == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "codSiruta must not be null");
        }
        return uatRepository.findByCodSiruta(codSiruta)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "UAT with codSiruta " + codSiruta + " not found."));
    }

    public Uat updateUat(String codSiruta, Uat request) {
        Uat existing = getUatByCodSiruta(codSiruta);

        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "UAT not found");
        }

        if (request.getDenumire() != null) {
            existing.setDenumire(request.getDenumire());
        }
        if (request.getJudet() != null) {
            existing.setJudet(request.getJudet());
        }
        if (request.getTipUat() != null) {
            existing.setTipUat(request.getTipUat());
        }
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }

        return uatRepository.save(existing);
    }

    public void deleteUat(String codSiruta) {
        Uat existing = getUatByCodSiruta(codSiruta);
        if (existing != null) {
            uatRepository.delete(existing);
        }
    }
}
