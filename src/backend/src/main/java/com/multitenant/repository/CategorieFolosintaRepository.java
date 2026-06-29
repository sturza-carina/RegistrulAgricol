package com.multitenant.repository;

import com.multitenant.model.registru.CategorieFolosinta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface CategorieFolosintaRepository extends JpaRepository<CategorieFolosinta, Long> {

    @Query("SELECT c FROM CategorieFolosinta c WHERE c.teren.id = :terenId")
    Page<CategorieFolosinta> findByTerenId(@Param("terenId") Long terenId, Pageable pageable);
}
