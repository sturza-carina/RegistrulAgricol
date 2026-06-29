package com.multitenant.service;

import com.multitenant.dto.CladireDTO;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CladireService {
    Page<CladireDTO> getCladiriByGospodarieId(Long gospodarieId, Pageable pageable);
    CladireDTO createCladire(Long gospodarieId, CladireDTO dto);
    CladireDTO updateCladire(Long id, CladireDTO dto);
    void deleteCladire(Long id);
}
