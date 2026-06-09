package com.multitenant.repository;

import com.multitenant.model.Uat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UatRepository extends JpaRepository<Uat, Long> {
    Optional<Uat> findByCodSiruta(String codSiruta);
    boolean existsByCodSiruta(String codSiruta);
    void deleteByCodSiruta(String codSiruta);
}
