package com.multitenant.repository;

import com.multitenant.model.registru.TipInregistrareVita;
import com.multitenant.model.registru.VitaDeVie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VitaDeVieRepository extends JpaRepository<VitaDeVie, Long> {

    Page<VitaDeVie> findByParcela_Id(Long parcelaId, Pageable pageable);

    Page<VitaDeVie> findByParcela_IdAndTipInregistrare(
            Long parcelaId, TipInregistrareVita tipInregistrare, Pageable pageable);
}
