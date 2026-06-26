package com.multitenant.repository;

import com.multitenant.model.core.Uat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UatRepository extends JpaRepository<Uat, Long> {
    Optional<Uat> findByCodSiruta(String codSiruta);
    boolean existsByCodSiruta(String codSiruta);
    void deleteByCodSiruta(String codSiruta);
    java.util.List<Uat> findByTenant_Id(String tenantId);
    java.util.List<Uat> findByTenant_IdOrTenantIsNull(String tenantId);

    @Query("select distinct u.judet from Uat u order by u.judet asc")
    java.util.List<String> findDistinctJudeteOrderByJudetAsc();

    java.util.List<Uat> findByJudetOrderByDenumireAsc(String judet);
}

