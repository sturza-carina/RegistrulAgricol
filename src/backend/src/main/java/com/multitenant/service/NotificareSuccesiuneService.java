package com.multitenant.service;

import com.multitenant.dto.NotificareSuccesiuneDTO;
import java.util.List;

public interface NotificareSuccesiuneService {
    NotificareSuccesiuneDTO save(NotificareSuccesiuneDTO dto);
    List<NotificareSuccesiuneDTO> getAll();
    List<NotificareSuccesiuneDTO> findByDefunctCnp(String cnpClar);
    NotificareSuccesiuneDTO updateStadiu(Long id, String noulStadiu);
}
