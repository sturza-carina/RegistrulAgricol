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

    /**
     * Returnează istoricul complet al snapshot-urilor de efectiv pentru o gospodărie,
     * ordonat descrescator după dată (cel mai recent întâi).
     */
    @Query("SELECT e FROM EfectivGrup e WHERE e.gospodarie.id = :gospodarieId ORDER BY e.dataInregistrare DESC")
    List<EfectivGrup> findByGospodarieIdOrderByDataInregistrareDesc(@Param("gospodarieId") Long gospodarieId);

    /**
     * Returnează cel mai recent snapshot pentru fiecare specie dintr-o gospodărie.
     * Subquery-ul selectează MAX(dataInregistrare) per (gospodarie, specie),
     * asigurând că UI-ul afișează starea curentă corectă per specie.
     */
    @Query("""
            SELECT e FROM EfectivGrup e
            WHERE e.gospodarie.id = :gospodarieId
              AND e.dataInregistrare = (
                  SELECT MAX(e2.dataInregistrare)
                  FROM EfectivGrup e2
                  WHERE e2.gospodarie.id = e.gospodarie.id
                    AND e2.specie = e.specie
              )
            ORDER BY e.specie
            """)
    List<EfectivGrup> findLatestSnapshotByGospodarieId(@Param("gospodarieId") Long gospodarieId);
}
