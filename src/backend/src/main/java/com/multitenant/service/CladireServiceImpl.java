package com.multitenant.service;

import com.multitenant.dto.CladireDTO;
import com.multitenant.model.registru.Cladire;
import com.multitenant.model.registru.Gospodarie;
import com.multitenant.model.registru.Teren;
import com.multitenant.repository.CladireRepository;
import com.multitenant.repository.GospodarieRepository;
import com.multitenant.repository.TerenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class CladireServiceImpl implements CladireService {

    private final CladireRepository cladireRepository;
    private final GospodarieRepository gospodarieRepository;
    private final TerenRepository terenRepository;

    public CladireServiceImpl(CladireRepository cladireRepository,
                              GospodarieRepository gospodarieRepository,
                              TerenRepository terenRepository) {
        this.cladireRepository = cladireRepository;
        this.gospodarieRepository = gospodarieRepository;
        this.terenRepository = terenRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CladireDTO> getCladiriByGospodarieId(Long gospodarieId, Pageable pageable) {
        if (!gospodarieRepository.existsById(gospodarieId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Gospodarie not found");
        }
        return cladireRepository.findByGospodarieId(gospodarieId, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional
    public CladireDTO createCladire(Long gospodarieId, CladireDTO dto) {
        Gospodarie gospodarie = gospodarieRepository.findById(gospodarieId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gospodarie not found"));

        Cladire cladire = new Cladire();
        cladire.setGospodarie(gospodarie);
        
        mapToEntity(dto, cladire);

        Cladire saved = cladireRepository.save(cladire);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public CladireDTO updateCladire(Long id, CladireDTO dto) {
        Cladire cladire = cladireRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cladire not found"));

        mapToEntity(dto, cladire);

        Cladire saved = cladireRepository.save(cladire);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public void deleteCladire(Long id) {
        if (!cladireRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cladire not found");
        }
        cladireRepository.deleteById(id);
    }

    private CladireDTO mapToDTO(Cladire cladire) {
        CladireDTO dto = new CladireDTO();
        dto.setId(cladire.getId());
        dto.setDestinatie(cladire.getDestinatie());
        dto.setSuprafataConstruita(cladire.getSuprafataConstruita());
        dto.setAnTerminare(cladire.getAnTerminare());
        dto.setMateriale(cladire.getMateriale());
        dto.setAdresaSauParcela(cladire.getAdresaSauParcela());
        dto.setGospodarieId(cladire.getGospodarieId());
        dto.setTerenId(cladire.getTerenId());
        return dto;
    }

    private void mapToEntity(CladireDTO dto, Cladire cladire) {
        cladire.setDestinatie(dto.getDestinatie());
        cladire.setSuprafataConstruita(dto.getSuprafataConstruita());
        cladire.setAnTerminare(dto.getAnTerminare());
        cladire.setMateriale(dto.getMateriale());
        cladire.setAdresaSauParcela(dto.getAdresaSauParcela());

        if (dto.getTerenId() != null) {
            Teren teren = terenRepository.findById(dto.getTerenId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teren not found"));
            cladire.setTeren(teren);
        } else {
            cladire.setTeren(null);
        }
    }
}
