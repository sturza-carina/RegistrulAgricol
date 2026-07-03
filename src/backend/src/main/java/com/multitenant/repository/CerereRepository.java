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
    List<Cerere> findByCnpCui(String cnpCui);
}
