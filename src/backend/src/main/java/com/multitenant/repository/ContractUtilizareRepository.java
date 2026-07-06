package com.multitenant.repository;

import com.multitenant.model.registru.ContractUtilizare;
import com.multitenant.model.registru.StatusContractUtilizare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractUtilizareRepository extends JpaRepository<ContractUtilizare, Long> {

    @NonNull
    @EntityGraph(attributePaths = {"parcela", "locatorProprietar", "locatorUtilizator"})
    Page<ContractUtilizare> findAll(@NonNull Pageable pageable);

    @NonNull
    @EntityGraph(attributePaths = {"parcela", "locatorProprietar", "locatorUtilizator"})
    Optional<ContractUtilizare> findById(@NonNull Long id);

    @EntityGraph(attributePaths = {"parcela", "locatorProprietar", "locatorUtilizator"})
    Page<ContractUtilizare> findByParcelaId(Long parcelaId, Pageable pageable);

    @EntityGraph(attributePaths = {"parcela", "locatorProprietar", "locatorUtilizator"})
    @Query("SELECT c FROM ContractUtilizare c WHERE c.parcela.teren.gospodarie.uat.codSiruta = :uatCode")
    Page<ContractUtilizare> findByUatCode(@Param("uatCode") String uatCode, Pageable pageable);

    @Modifying
    @Query("UPDATE ContractUtilizare c " +
           "SET c.statusContract = :expiredStatus, c.esteActiv = false " +
           "WHERE c.statusContract = :activeStatus " +
           "AND c.dataSfarsit IS NOT NULL " +
           "AND c.dataSfarsit < :currentDate")
    int markExpiredContracts(@Param("activeStatus") StatusContractUtilizare activeStatus,
                             @Param("expiredStatus") StatusContractUtilizare expiredStatus,
                             @Param("currentDate") LocalDate currentDate);

    List<ContractUtilizare> findByStatusContractAndDataSfarsitLessThan(StatusContractUtilizare statusContract, LocalDate dataSfarsit);
}
