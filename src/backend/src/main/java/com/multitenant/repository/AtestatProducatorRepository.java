package com.multitenant.repository;

import com.multitenant.model.registru.AtestatProducator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface AtestatProducatorRepository extends JpaRepository<AtestatProducator, Long> {
    List<AtestatProducator> findByPersoanaId(Long persoanaId);
    
    @Query(value = "SELECT a.* FROM atestate_producator a JOIN persons p ON a.persoana_id = p.id WHERE p.person_type = 'PHYSICAL_PERSON' AND p.cnp_hash = :cnpHash AND a.deleted = false", nativeQuery = true)
    List<AtestatProducator> findByCnpHash(@Param("cnpHash") String cnpHash);
}