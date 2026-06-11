package com.multitenant.repository;

import com.multitenant.model.registru.Parcela;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ParcelaRepository extends JpaRepository<Parcela, Long> {
    @Query("SELECT p FROM Parcela p WHERE p.teren.id = :terenId")
    List<Parcela> findByTerenId(@Param("terenId") Long terenId);
}
