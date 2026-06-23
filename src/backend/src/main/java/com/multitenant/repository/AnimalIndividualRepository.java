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

    /** Returnează toate animalele individuale dintr-o gospodărie — necesar pentru view-ul gospodărie-details. */
    @Query("SELECT a FROM AnimalIndividual a WHERE a.gospodarie.id = :gospodarieId")
    List<AnimalIndividual> findByGospodarieId(@Param("gospodarieId") Long gospodarieId);

    /**
     * Verifică dacă există un alt animal cu același crotal în această schemă (intra-tenant).
     * Excludem animalul curent (pentru update) prin clauza AND id != excludeId.
     */
    @Query("SELECT COUNT(a) > 0 FROM AnimalIndividual a WHERE a.numarCrotal = :crotal AND a.stareActiva = true AND (:excludeId IS NULL OR a.id != :excludeId)")
    boolean existsByNumarCrotalAndIdNot(@Param("crotal") String crotal, @Param("excludeId") Long excludeId);
}
