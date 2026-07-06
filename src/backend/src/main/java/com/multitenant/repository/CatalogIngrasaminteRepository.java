package com.multitenant.repository;

import com.multitenant.model.registru.CatalogIngrasaminte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CatalogIngrasaminteRepository extends JpaRepository<CatalogIngrasaminte, Long> {
    Page<CatalogIngrasaminte> findByDenumireContainingIgnoreCase(String query, Pageable pageable);
}
