package com.multitenant.repository;

import com.multitenant.model.registru.CarteFunciara;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarteFunciaraRepository extends JpaRepository<CarteFunciara, Long> {

    /**
     * Gaseste CarteFunciara asociata unui Teren dupa teren_id.
     * Folosita de listener pentru a actualiza suprafata la adaugarea parcelelor.
     *
     * NOTA: Spring Data JPA nu poate deriva query-ul din 'findByTerenId' deoarece
     * campul se numeste 'teren' (asociere @OneToOne), nu 'terenId'.
     * Se foloseste @Query explicit cu traversal JPQL cf.teren.id.
     */
    @Query("SELECT cf FROM CarteFunciara cf WHERE cf.teren.id = :terenId")
    Optional<CarteFunciara> findByTerenId(@Param("terenId") Long terenId);

    /**
     * Calculeaza suma suprafetelor tuturor parcelelor unui teren.
     * Rulata de listener dupa fiecare adaugare de parcela pentru a actualiza
     * suprafata_totala_intabulata fara a incarca toate entitatile in memorie.
     */
    @Query("SELECT COALESCE(SUM(p.suprafata), 0.0) FROM Parcela p WHERE p.teren.id = :terenId")
    Double sumSuprafataByTerenId(@Param("terenId") Long terenId);
}
