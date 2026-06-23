package com.multitenant.repository;

import com.multitenant.model.registru.CulturaParcela;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CulturaParcelaRepository extends JpaRepository<CulturaParcela, Long> {

    List<CulturaParcela> findByParcela_Id(Long parcelaId);

}
