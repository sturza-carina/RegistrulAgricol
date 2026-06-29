package com.multitenant.repository;

import com.multitenant.model.registru.Gospodarie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface GospodarieRepository extends JpaRepository<Gospodarie, Long> {
    Optional<Gospodarie> findByCodGospodarie(String codGospodarie);

    Page<Gospodarie> findByUat_CodSirutaOrderByIdDesc(String codSiruta, Pageable pageable);

    Page<Gospodarie> findAllByOrderByIdDesc(Pageable pageable);
}
