package com.multitenant.repository;

import com.multitenant.model.registru.CategorieFolosinta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategorieFolosintaRepository extends JpaRepository<CategorieFolosinta, Long> {

    @Query("SELECT c FROM CategorieFolosinta c WHERE c.teren.id = :terenId")
    List<CategorieFolosinta> findByTerenId(@Param("terenId") Long terenId);
}
