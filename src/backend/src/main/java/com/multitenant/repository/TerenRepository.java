package com.multitenant.repository;

import com.multitenant.model.registru.Teren;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface TerenRepository extends JpaRepository<Teren, Long> {
    @Query("SELECT t FROM Teren t WHERE t.gospodarie.id = :gospodarieId")
    Optional<Teren> findByGospodarieId(@Param("gospodarieId") Long gospodarieId);
}
