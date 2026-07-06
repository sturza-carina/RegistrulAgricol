package com.multitenant.repository;

import com.multitenant.dto.StatisticaCulturaDto;
import com.multitenant.model.registru.CulturaParcela;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface CulturaParcelaRepository extends JpaRepository<CulturaParcela, Long> {

    Page<CulturaParcela> findByParcela_Id(Long parcelaId, Pageable pageable);

    @Query("SELECT new com.multitenant.dto.StatisticaCulturaDto(c.specieCultura, SUM(c.suprafataCultivataHa), SUM(c.productieTotalaTone)) " +
           "FROM CulturaParcela c " +
           "WHERE c.anAgricol = :an " +
           "GROUP BY c.specieCultura")
    List<StatisticaCulturaDto> getStatisticiCulturi(@Param("an") Integer an);

}

