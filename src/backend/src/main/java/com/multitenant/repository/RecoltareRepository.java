package com.multitenant.repository;

import com.multitenant.model.registru.Recoltare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface RecoltareRepository extends JpaRepository<Recoltare, Long> {

    Page<Recoltare> findByParcela_Id(Long parcelaId, Pageable pageable);

    List<Recoltare> findByParcela_Id(Long parcelaId);

    List<Recoltare> findByCicluProductie_Id(Long cicluProductieId);

    @Query("SELECT r FROM Recoltare r WHERE r.parcela.teren.id = :terenId")
    Page<Recoltare> findByTerenId(@Param("terenId") Long terenId, Pageable pageable);

    @Query("SELECT r FROM Recoltare r WHERE EXTRACT(YEAR FROM r.dataRecoltare) = :an")
    List<Recoltare> findAllByAnAgricol(@Param("an") Integer an);
}
