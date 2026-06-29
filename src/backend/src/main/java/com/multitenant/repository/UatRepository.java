package com.multitenant.repository;

import com.multitenant.model.core.Uat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UatRepository extends JpaRepository<Uat, Long> {
    Optional<Uat> findByCodSiruta(String codSiruta);
    boolean existsByCodSiruta(String codSiruta);
    void deleteByCodSiruta(String codSiruta);

    @Query("select distinct u.judet from Uat u order by u.judet asc")
    List<String> findDistinctJudeteOrderByJudetAsc();

    List<Uat> findByJudetOrderByDenumireAsc(String judet);
}
