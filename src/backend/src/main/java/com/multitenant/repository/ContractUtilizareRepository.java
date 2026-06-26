package com.multitenant.repository;

import com.multitenant.model.registru.ContractUtilizare;
import com.multitenant.model.registru.StatusContractUtilizare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDate;

@Repository
public interface ContractUtilizareRepository extends JpaRepository<ContractUtilizare, Long> {
    List<ContractUtilizare> findByTerenId(Long terenId);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ContractUtilizare c " +
           "WHERE c.teren.id = :terenId " +
           "AND c.statusContract = :status " +
           "AND (:endDate IS NULL OR c.dataInceput <= :endDate) " +
           "AND (c.dataSfarsit IS NULL OR c.dataSfarsit >= :startDate) " +
           "AND (:excludeId IS NULL OR c.id <> :excludeId)")
    boolean existsActiveOverlap(@Param("terenId") Long terenId,
                                @Param("status") StatusContractUtilizare status,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate,
                                @Param("excludeId") Long excludeId);

    @Modifying
    @Query("UPDATE ContractUtilizare c " +
           "SET c.statusContract = :expiredStatus, c.esteActiv = false " +
           "WHERE c.statusContract = :activeStatus " +
           "AND c.dataSfarsit IS NOT NULL " +
           "AND c.dataSfarsit < :currentDate")
    int markExpiredContracts(@Param("activeStatus") StatusContractUtilizare activeStatus,
                             @Param("expiredStatus") StatusContractUtilizare expiredStatus,
                             @Param("currentDate") LocalDate currentDate);
}
