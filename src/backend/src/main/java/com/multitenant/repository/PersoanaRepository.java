package com.multitenant.repository;

import com.multitenant.model.persoana.Persoana;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
           "   TREAT(p AS PersoanaFizica).cnpHash = :searchHash " +
           " )) OR " +
           " (TYPE(p) = PersoanaJuridica AND (" +
           "   LOWER(COALESCE(TREAT(p AS PersoanaJuridica).companyName, '')) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) OR " +
           "   TREAT(p AS PersoanaJuridica).cuiHash = :searchHash " +
           " )))")
    Page<Persoana> searchPersons(@Param("search") String search, @Param("searchHash") String searchHash, @Param("type") String type, Pageable pageable);

    @Query("SELECT p FROM Persoana p JOIN p.gospodarii g WHERE g.id = :gospodarieId")
    Page<Persoana> findByGospodarieId(@Param("gospodarieId") Long gospodarieId, Pageable pageable);

    Page<Persoana> findByPersonTypeOrderByIdDesc(String personType, Pageable pageable);

    @Query("SELECT p FROM Persoana p WHERE TYPE(p) = PersoanaFizica AND TREAT(p AS PersoanaFizica).cnpHash = :cnpHash")
    java.util.Optional<Persoana> findByCnpHash(@Param("cnpHash") String cnpHash);

    default java.util.Optional<Persoana> findByCnpClar(String cnp) {
        if (cnp == null || cnp.trim().isEmpty()) {
            return java.util.Optional.empty();
        }
        return findByCnpHash(com.multitenant.util.CryptoUtils.hashSha256(cnp.trim()));
    }

    default java.util.Optional<Persoana> findByCnp(String cnp) {
        return findByCnpClar(cnp);
    }

    @Query("SELECT p FROM Persoana p WHERE TYPE(p) = PersoanaJuridica AND TREAT(p AS PersoanaJuridica).cuiHash = :cuiHash")
    java.util.Optional<Persoana> findByCuiHash(@Param("cuiHash") String cuiHash);

    default java.util.Optional<Persoana> findByCuiClar(String cui) {
        if (cui == null || cui.trim().isEmpty()) {
            return java.util.Optional.empty();
        }
        return findByCuiHash(com.multitenant.util.CryptoUtils.hashSha256(cui.trim()));
    }
}

