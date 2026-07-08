package com.multitenant.repository;

import com.multitenant.model.registru.CicluProductie;
import com.multitenant.model.registru.CicluStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CicluProductieRepository extends JpaRepository<CicluProductie, Long> {

    Page<CicluProductie> findByParcela_Id(Long parcelaId, Pageable pageable);

    List<CicluProductie> findByParcela_Id(Long parcelaId);

    Optional<CicluProductie> findFirstByParcela_IdAndStatus(Long parcelaId, CicluStatus status);

    @Query("SELECT c FROM CicluProductie c WHERE c.parcela.id = :parcelaId " +
           "AND c.dataInfiintare <= :date " +
           "AND (c.dataDefisare IS NULL OR c.dataDefisare >= :date)")
    List<CicluProductie> findActiveCyclesOnDate(@Param("parcelaId") Long parcelaId, @Param("date") LocalDate date);
}
