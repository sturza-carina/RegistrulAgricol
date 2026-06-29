package com.multitenant.repository;

import com.multitenant.model.registru.SursaApa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SursaApaRepository extends JpaRepository<SursaApa, Long> {
    Page<SursaApa> findByParcela_Id(Long parcelaId, Pageable pageable);
}
