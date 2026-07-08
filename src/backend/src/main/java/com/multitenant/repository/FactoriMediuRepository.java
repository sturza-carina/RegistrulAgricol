package com.multitenant.repository;

import com.multitenant.model.registru.FactoriMediu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FactoriMediuRepository extends JpaRepository<FactoriMediu, Long> {
    Page<FactoriMediu> findByParcela_Id(Long parcelaId, Pageable pageable);
}
