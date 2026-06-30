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

    @Modifying
    @Query("UPDATE ContractUtilizare c " +
           "SET c.statusContract = :expiredStatus, c.esteActiv = false " +
           "WHERE c.statusContract = :activeStatus " +
           "AND c.dataSfarsit IS NOT NULL " +
           "AND c.dataSfarsit < :currentDate")
    int markExpiredContracts(@Param("activeStatus") StatusContractUtilizare activeStatus,
                             @Param("expiredStatus") StatusContractUtilizare expiredStatus,
                             @Param("currentDate") LocalDate currentDate);

    @Query("SELECT COUNT(c) > 0 FROM ContractUtilizare c " +
           "WHERE c.parcela.id = :parcelaId " +
           "AND c.id <> :excludeId " +
           "AND c.statusContract <> com.multitenant.model.registru.StatusContractUtilizare.EXPIRAT " +
           "AND c.esteActiv = true " +
           "AND (c.dataSfarsit IS NULL OR c.dataSfarsit >= :dataInceput) " +
           "AND (c.dataInceput IS NULL OR c.dataInceput <= :dataSfarsit)")
    boolean existsOverlappingContract(@Param("parcelaId") Long parcelaId,
                                      @Param("excludeId") Long excludeId,
                                      @Param("dataInceput") LocalDate dataInceput,
                                      @Param("dataSfarsit") LocalDate dataSfarsit);

    @Query("SELECT COUNT(c) > 0 FROM ContractUtilizare c " +
           "WHERE c.teren.id = :terenId " +
           "AND c.parcela IS NULL " +
           "AND c.id <> :excludeId " +
           "AND c.statusContract <> com.multitenant.model.registru.StatusContractUtilizare.EXPIRAT " +
           "AND c.esteActiv = true " +
           "AND (c.dataSfarsit IS NULL OR c.dataSfarsit >= :dataInceput) " +
           "AND (c.dataInceput IS NULL OR c.dataInceput <= :dataSfarsit)")
    boolean existsOverlappingContractByTeren(@Param("terenId") Long terenId,
                                             @Param("excludeId") Long excludeId,
                                             @Param("dataInceput") LocalDate dataInceput,
                                             @Param("dataSfarsit") LocalDate dataSfarsit);
}
