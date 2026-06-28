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

    @Query("SELECT c FROM ContractUtilizare c WHERE c.teren.gospodarie.uat.codSiruta = :uatCode")
    List<ContractUtilizare> findByUatCode(@Param("uatCode") String uatCode);

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
