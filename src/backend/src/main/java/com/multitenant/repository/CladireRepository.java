package com.multitenant.repository;

import com.multitenant.model.registru.Cladire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface CladireRepository extends JpaRepository<Cladire, Long> {
    @Query("SELECT c FROM Cladire c WHERE c.gospodarie.id = :gospodarieId")
    List<Cladire> findByGospodarieId(@Param("gospodarieId") Long gospodarieId);
}
