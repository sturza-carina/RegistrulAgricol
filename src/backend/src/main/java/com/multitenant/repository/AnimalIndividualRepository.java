package com.multitenant.repository;

import com.multitenant.model.animal.AnimalIndividual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface AnimalIndividualRepository extends JpaRepository<AnimalIndividual, Long> {

    @Query("SELECT a FROM AnimalIndividual a WHERE a.proprietar.id = :proprietarId")
    Page<AnimalIndividual> findByProprietarId(@Param("proprietarId") Long proprietarId, Pageable pageable);

    /** Returnează toate animalele individuale dintr-o gospodărie — necesar pentru view-ul gospodărie-details. */
    @Query("SELECT a FROM AnimalIndividual a WHERE a.gospodarie.id = :gospodarieId")
    Page<AnimalIndividual> findByGospodarieId(@Param("gospodarieId") Long gospodarieId, Pageable pageable);

    /**
     * Verifică dacă există un alt animal cu același crotal în această schemă (intra-tenant).
     * Excludem animalul curent (pentru update) prin clauza AND id != excludeId.
     */
    @Query("SELECT COUNT(a) > 0 FROM AnimalIndividual a WHERE a.numarCrotal = :crotal AND a.stareActiva = true AND (:excludeId IS NULL OR a.id != :excludeId)")
    boolean existsByNumarCrotalAndIdNot(@Param("crotal") String crotal, @Param("excludeId") Long excludeId);

    @Query("SELECT new com.multitenant.dto.StatisticaAnimalDto(a.specie, COUNT(a), " +
           "SUM(CASE WHEN a.sex = com.multitenant.model.animal.SexAnimal.MASCULIN THEN 1L ELSE 0L END), " +
           "SUM(CASE WHEN a.sex = com.multitenant.model.animal.SexAnimal.FEMININ THEN 1L ELSE 0L END)) " +
           "FROM AnimalIndividual a " +
           "WHERE a.stareActiva = true " +
           "GROUP BY a.specie")
    List<com.multitenant.dto.StatisticaAnimalDto> getStatisticiAnimaleIndividuale();
}

