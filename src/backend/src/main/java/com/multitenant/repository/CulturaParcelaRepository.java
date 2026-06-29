package com.multitenant.repository;

import com.multitenant.model.registru.CulturaParcela;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface CulturaParcelaRepository extends JpaRepository<CulturaParcela, Long> {

    Page<CulturaParcela> findByParcela_Id(Long parcelaId, Pageable pageable);

}
