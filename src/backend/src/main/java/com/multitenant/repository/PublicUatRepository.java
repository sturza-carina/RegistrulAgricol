package com.multitenant.repository;

import com.multitenant.model.core.PublicUat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for the global public.uat table.
 * Always operates in the public schema (enforced by schema="public" on PublicUat entity).
 */
@Repository
public interface PublicUatRepository extends JpaRepository<PublicUat, Long> {
    Optional<PublicUat> findByCodSiruta(String codSiruta);
    boolean existsByCodSiruta(String codSiruta);

    List<PublicUat> findByTenantId(String tenantId);
    List<PublicUat> findByTenantIdIsNull();
    List<PublicUat> findByTenantIdOrTenantIdIsNull(String tenantId);
    List<PublicUat> findByJudetOrderByDenumireAsc(String judet);

    @Query("select distinct u.judet from PublicUat u order by u.judet asc")
    List<String> findDistinctJudeteOrderByJudetAsc();
}
