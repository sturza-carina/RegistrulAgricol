package com.multitenant.service;

import com.multitenant.dto.MachineryDTO;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MachineryService {
    Page<MachineryDTO> getAll(Pageable pageable);

    Page<MachineryDTO> getAllByGospodarie(Long id, Pageable pageable);

    MachineryDTO getById(Long id);

    MachineryDTO create(MachineryDTO dto);

    MachineryDTO update(Long id, MachineryDTO dto);

    void delete(Long id);
}
