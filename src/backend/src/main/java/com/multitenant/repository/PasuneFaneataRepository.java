package com.multitenant.repository;

import com.multitenant.model.registru.PasuneFaneata;
import com.multitenant.model.registru.TipFolosintaPasune;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasuneFaneataRepository extends JpaRepository<PasuneFaneata, Long> {

    Page<PasuneFaneata> findByParcela_Id(Long parcelaId, Pageable pageable);

    Page<PasuneFaneata> findByParcela_IdAndTipFolosinta(
            Long parcelaId, TipFolosintaPasune tipFolosinta, Pageable pageable);
}
