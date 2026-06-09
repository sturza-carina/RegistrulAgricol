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
           "(:type IS NULL OR p.class = :type) AND " +
           "(:search IS NULL OR " +
           " LOWER(p.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(p.phoneNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " (TYPE(p) = PhysicalPerson AND (" +
           "   LOWER(TREAT(p AS PhysicalPerson).firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "   LOWER(TREAT(p AS PhysicalPerson).lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "   TREAT(p AS PhysicalPerson).cnp LIKE CONCAT('%', :search, '%') " +
           " )) OR " +
           " (TYPE(p) = LegalEntity AND (" +
           "   LOWER(TREAT(p AS LegalEntity).companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "   TREAT(p AS LegalEntity).cui LIKE CONCAT('%', :search, '%') " +
           " )))")
    List<Person> searchPersons(@Param("search") String search, @Param("type") String type);
}
