package com.multitenant.repository;

import com.multitenant.model.registru.Cladire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface CladireRepository extends JpaRepository<Cladire, Long> {
    @Query("SELECT c FROM Cladire c WHERE c.gospodarie.id = :gospodarieId")
    Page<Cladire> findByGospodarieId(@Param("gospodarieId") Long gospodarieId, Pageable pageable);
}
