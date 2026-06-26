package com.multitenant.service;

import com.multitenant.dto.CladireDTO;
import java.util.List;

public interface CladireService {
    List<CladireDTO> getCladiriByGospodarieId(Long gospodarieId);
    CladireDTO createCladire(Long gospodarieId, CladireDTO dto);
    CladireDTO updateCladire(Long id, CladireDTO dto);
    void deleteCladire(Long id);
}
