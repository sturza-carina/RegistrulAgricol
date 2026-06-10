package com.multitenant.repository;

import com.multitenant.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    @Query("SELECT p FROM Person p WHERE " +
           "(CAST(:type AS text) IS NULL OR p.personType = CAST(:type AS text)) AND " +
           "(CAST(:search AS text) IS NULL OR " +
           " LOWER(COALESCE(p.email, '')) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) OR " +
           " LOWER(COALESCE(p.phoneNumber, '')) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) OR " +
           " (TYPE(p) = PhysicalPerson AND (" +
           "   LOWER(COALESCE(TREAT(p AS PhysicalPerson).firstName, '')) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) OR " +
           "   LOWER(COALESCE(TREAT(p AS PhysicalPerson).lastName, '')) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) OR " +
           "   COALESCE(TREAT(p AS PhysicalPerson).cnp, '') LIKE CONCAT('%', CAST(:search AS text), '%') " +
           " )) OR " +
           " (TYPE(p) = LegalEntity AND (" +
           "   LOWER(COALESCE(TREAT(p AS LegalEntity).companyName, '')) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) OR " +
           "   COALESCE(TREAT(p AS LegalEntity).cui, '') LIKE CONCAT('%', CAST(:search AS text), '%') " +
           " )))")
    List<Person> searchPersons(@Param("search") String search, @Param("type") String type);
}
