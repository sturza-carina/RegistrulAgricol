package com.multitenant.repository;

import com.multitenant.model.animal.EfectivGrup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EfectivGrupRepository extends JpaRepository<EfectivGrup, Long> {

    @Query("SELECT e FROM EfectivGrup e WHERE e.proprietar.id = :proprietarId")
    List<EfectivGrup> findByProprietarId(@Param("proprietarId") Long proprietarId);
}
