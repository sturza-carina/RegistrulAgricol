package com.multitenant.service;

import com.multitenant.repository.LookupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LookupService {

    private final LookupRepository lookupRepository;

    public List<String> getTipuriSol() {
        return lookupRepository.findAllTipuriSol();
    }

    public List<String> getCategoriiFolosinta() {
        return lookupRepository.findAllCategoriiFolosinta();
    }

    public List<String> getTipuriSursaApa() {
        return lookupRepository.findAllTipuriSursaApa();
    }
}