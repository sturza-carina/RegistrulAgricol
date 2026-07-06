package com.multitenant.repository;

import com.multitenant.model.registru.Machinery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface MachineryRepository extends JpaRepository<Machinery, Long> {
    @Query("SELECT m FROM Machinery m WHERE m.gospodarie.id = :gospodarieId")
    Page<Machinery> findByGospodarieId(@Param("gospodarieId") Long gospodarieId, Pageable pageable);

    @Query("SELECT new com.multitenant.dto.StatisticaUtilajDto(m.tipUtilaj, COUNT(m)) " +
           "FROM Machinery m " +
           "WHERE (:uatCode IS NULL OR m.gospodarie.uat.codSiruta = :uatCode) " +
           "GROUP BY m.tipUtilaj")
    List<com.multitenant.dto.StatisticaUtilajDto> getStatisticiUtilaje(@Param("uatCode") String uatCode);
}

