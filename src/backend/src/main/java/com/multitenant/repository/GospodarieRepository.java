package com.multitenant.repository;

import com.multitenant.model.registru.Gospodarie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GospodarieRepository extends JpaRepository<Gospodarie, Long> {
    Optional<Gospodarie> findByCodGospodarie(String codGospodarie);

    List<Gospodarie> findByUat_CodSirutaOrderByIdDesc(String codSiruta);

    List<Gospodarie> findAllByOrderByIdDesc();
}
