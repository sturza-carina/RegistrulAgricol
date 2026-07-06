package com.multitenant.repository;

import com.multitenant.dto.StatisticaCategorieFolosintaDto;
import com.multitenant.model.registru.Parcela;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ParcelaRepository extends JpaRepository<Parcela, Long> {
    @Query("SELECT p FROM Parcela p WHERE p.teren.id = :terenId")
    Page<Parcela> findByTerenId(@Param("terenId") Long terenId, Pageable pageable);

    @Query("SELECT new com.multitenant.dto.StatisticaCategorieFolosintaDto(p.categorieFolosinta, SUM(p.suprafata)) " +
           "FROM Parcela p " +
           "WHERE (:uatCode IS NULL OR p.teren.gospodarie.uat.codSiruta = :uatCode) " +
           "GROUP BY p.categorieFolosinta")
    List<StatisticaCategorieFolosintaDto> getStatisticiCategoriiFolosinta(@Param("uatCode") String uatCode);
}

