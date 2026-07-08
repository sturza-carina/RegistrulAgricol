package com.multitenant.repository;

import com.multitenant.model.registru.Padure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PadureRepository extends JpaRepository<Padure, Long> {
    Page<Padure> findByParcela_Id(Long parcelaId, Pageable pageable);
}
