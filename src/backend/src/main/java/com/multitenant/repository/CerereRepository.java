package com.multitenant.repository;

import com.multitenant.model.registru.Cerere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CerereRepository extends JpaRepository<Cerere, Long> {
    Optional<Cerere> findByCodCerere(String codCerere);
    List<Cerere> findByUatId(Long uatId);
    List<Cerere> findByUserId(Long userId);
    List<Cerere> findByCnpCuiHash(String cnpCuiHash);

    default List<Cerere> findByCnpCuiClar(String cnpCui) {
        if (cnpCui == null || cnpCui.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return findByCnpCuiHash(com.multitenant.util.CryptoUtils.hashSha256(cnpCui.trim()));
    }

    default List<Cerere> findByCnpCui(String cnpCui) {
        return findByCnpCuiClar(cnpCui);
    }
}
