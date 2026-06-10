package com.multitenant.repository;

import com.multitenant.model.Persoana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersoanaRepository extends JpaRepository<Persoana, Long> {

    @Query("SELECT p FROM Persoana p WHERE " +
           "(CAST(:type AS text) IS NULL OR p.personType = CAST(:type AS text)) AND " +
           "(CAST(:search AS text) IS NULL OR " +
           " LOWER(COALESCE(p.email, '')) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) OR " +
           " LOWER(COALESCE(p.phoneNumber, '')) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) OR " +
           " (TYPE(p) = PersoanaFizica AND (" +
           "   LOWER(COALESCE(TREAT(p AS PersoanaFizica).firstName, '')) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) OR " +
           "   LOWER(COALESCE(TREAT(p AS PersoanaFizica).lastName, '')) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) OR " +
           "   COALESCE(TREAT(p AS PersoanaFizica).cnp, '') LIKE CONCAT('%', CAST(:search AS text), '%') " +
           " )) OR " +
           " (TYPE(p) = PersoanaJuridica AND (" +
           "   LOWER(COALESCE(TREAT(p AS PersoanaJuridica).companyName, '')) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) OR " +
           "   COALESCE(TREAT(p AS PersoanaJuridica).cui, '') LIKE CONCAT('%', CAST(:search AS text), '%') " +
           " )))")
    List<Persoana> searchPersons(@Param("search") String search, @Param("type") String type);
}

