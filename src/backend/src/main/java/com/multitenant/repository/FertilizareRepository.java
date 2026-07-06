package com.multitenant.repository;

import com.multitenant.model.registru.Fertilizare;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FertilizareRepository extends JpaRepository<Fertilizare, Long> {
    Page<Fertilizare> findByParcela_Id(Long parcelaId, Pageable pageable);
    List<Fertilizare> findByParcela_Id(Long parcelaId);

    @Query("SELECT f FROM Fertilizare f JOIN FETCH f.parcela p WHERE p.id = :parcelaId ORDER BY f.dataAplicarii DESC")
    List<Fertilizare> findByParcelaIdWithRelations(@Param("parcelaId") Long parcelaId);

    @Query("SELECT f FROM Fertilizare f JOIN FETCH f.parcela p JOIN FETCH f.catalogIngrasaminte ci ORDER BY f.dataAplicarii DESC")
    List<Fertilizare> findAllWithRelations();

    @Query("SELECT COALESCE(SUM(f.aportAzot), 0.0) FROM Fertilizare f " +
           "WHERE f.parcela.id = :parcelaId " +
           "AND f.dataAplicarii >= :startDate " +
           "AND f.dataAplicarii <= :endDate")
    Double sumAportAzotByParcelaAndDateRange(
            @Param("parcelaId") Long parcelaId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
