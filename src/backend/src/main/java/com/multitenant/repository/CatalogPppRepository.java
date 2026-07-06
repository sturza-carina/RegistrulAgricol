package com.multitenant.repository;

import com.multitenant.model.registru.CatalogPpp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CatalogPppRepository extends JpaRepository<CatalogPpp, Long> {
    Page<CatalogPpp> findByDenumireComercialaContainingIgnoreCase(String query, Pageable pageable);
}
