package com.multitenant.repository;

import com.multitenant.model.registru.Pom;
import com.multitenant.model.registru.TipInregistrarePom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PomRepository extends JpaRepository<Pom, Long> {

    Page<Pom> findByParcela_Id(Long parcelaId, Pageable pageable);

    Page<Pom> findByParcela_IdAndTipInregistrare(
            Long parcelaId, TipInregistrarePom tipInregistrare, Pageable pageable);
}