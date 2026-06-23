package com.multitenant.repository;

import com.multitenant.model.animal.AnimalIndividual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnimalIndividualRepository extends JpaRepository<AnimalIndividual, Long> {

    @Query("SELECT a FROM AnimalIndividual a WHERE a.proprietar.id = :proprietarId")
    List<AnimalIndividual> findByProprietarId(@Param("proprietarId") Long proprietarId);
}
