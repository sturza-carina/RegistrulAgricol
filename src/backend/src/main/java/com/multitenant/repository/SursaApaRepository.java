package com.multitenant.repository;

import com.multitenant.model.registru.SursaApa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SursaApaRepository extends JpaRepository<SursaApa, Long> {
    List<SursaApa> findByParcela_Id(Long parcelaId);
}
