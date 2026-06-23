package com.multitenant.service;

import com.multitenant.dto.MachineryDTO;

import java.util.List;

public interface MachineryService {
    List<MachineryDTO> getAll();

    List<MachineryDTO> getAllByGospodarie(Long id);

    MachineryDTO getById(Long id);

    MachineryDTO create(MachineryDTO dto);

    MachineryDTO update(Long id, MachineryDTO dto);

    void delete(Long id);
}
