package com.multitenant.service;

import com.multitenant.dto.MachineryDTO;
import com.multitenant.model.registru.Gospodarie;
import com.multitenant.model.registru.Machinery;
import com.multitenant.repository.GospodarieRepository;
import com.multitenant.repository.MachineryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class MachineryServiceImpl implements MachineryService {

    private final MachineryRepository machineryRepository;
    private final GospodarieRepository gospodarieRepository;

    @Override
    public Page<MachineryDTO> getAll(Pageable pageable) {
        return machineryRepository.findAll(pageable)
                .map(this::toDto);
    }

    @Override
    public Page<MachineryDTO> getAllByGospodarie(Long id, Pageable pageable) {
        if (id == null) {
            throw new IllegalArgumentException("Gospodarie id cannot be null");
        }

        return machineryRepository.findByGospodarieId(id, pageable)
                .map(this::toDto);
    }

    @Override
    public MachineryDTO getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Machinery id cannot be null");
        }

        return machineryRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Machinery not found with id: " + id));
    }

    @Override
    @Transactional
    public MachineryDTO create(MachineryDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Machinery DTO cannot be null");
        }

        Gospodarie gospodarie = resolveGospodarie(dto.getGospodarieId());

        Machinery entity = new Machinery();
        entity.setTipUtilaj(dto.getTipUtilaj());
        entity.setMarca(dto.getMarca());
        entity.setModel(dto.getModel());
        entity.setAnFabricatie(dto.getAnFabricatie());
        entity.setNumarInmatriculare(dto.getNumarInmatriculare());
        entity.setGospodarie(gospodarie);

        return toDto(machineryRepository.save(entity));
    }

    @Override
    @Transactional
    public MachineryDTO update(Long id, MachineryDTO dto) {
        if (id == null) {
            throw new IllegalArgumentException("Machinery id cannot be null");
        }
        if (dto == null) {
            throw new IllegalArgumentException("Machinery DTO cannot be null");
        }

        Machinery existing = machineryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Machinery not found with id: " + id));

        if (dto.getTipUtilaj() != null) {
            existing.setTipUtilaj(dto.getTipUtilaj());
        }
        if (dto.getMarca() != null) {
            existing.setMarca(dto.getMarca());
        }
        if (dto.getModel() != null) {
            existing.setModel(dto.getModel());
        }
        if (dto.getAnFabricatie() != null) {
            existing.setAnFabricatie(dto.getAnFabricatie());
        }
        if (dto.getNumarInmatriculare() != null) {
            existing.setNumarInmatriculare(dto.getNumarInmatriculare());
        }

        if (dto.getGospodarieId() != null) {
            Gospodarie gospodarie = resolveGospodarie(dto.getGospodarieId());
            existing.setGospodarie(gospodarie);
        }

        return toDto(machineryRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Machinery id cannot be null");
        }

        if (!machineryRepository.existsById(id)) {
            throw new RuntimeException("Machinery not found with id: " + id);
        }

        machineryRepository.deleteById(id);
    }

    private Gospodarie resolveGospodarie(Long gospodarieId) {
        if (gospodarieId == null) {
            throw new IllegalArgumentException("gospodarieId is required");
        }

        return gospodarieRepository.findById(gospodarieId)
                .orElseThrow(() -> new RuntimeException("Gospodarie not found with id: " + gospodarieId));
    }

    private MachineryDTO toDto(Machinery entity) {
        MachineryDTO dto = new MachineryDTO();
        dto.setId(entity.getId());
        dto.setTipUtilaj(entity.getTipUtilaj());
        dto.setMarca(entity.getMarca());
        dto.setModel(entity.getModel());
        dto.setAnFabricatie(entity.getAnFabricatie());
        dto.setNumarInmatriculare(entity.getNumarInmatriculare());
        dto.setGospodarieId(entity.getGospodarieId());
        return dto;
    }
}
