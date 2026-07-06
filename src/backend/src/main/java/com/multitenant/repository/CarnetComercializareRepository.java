package com.multitenant.repository;

import com.multitenant.model.registru.CarnetComercializare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface CarnetComercializareRepository extends JpaRepository<CarnetComercializare, Long> {
    List<CarnetComercializare> findByPersoanaId(Long persoanaId);
    
    @Query(value = "SELECT c.* FROM carnete_comercializare c JOIN persons p ON c.persoana_id = p.id WHERE p.person_type = 'PHYSICAL_PERSON' AND p.cnp_hash = :cnpHash AND c.deleted = false", nativeQuery = true)
    List<CarnetComercializare> findByCnpHash(@Param("cnpHash") String cnpHash);
}