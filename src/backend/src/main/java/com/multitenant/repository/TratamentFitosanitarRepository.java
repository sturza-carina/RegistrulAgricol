package com.multitenant.repository;

import com.multitenant.model.registru.TratamentFitosanitar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TratamentFitosanitarRepository extends JpaRepository<TratamentFitosanitar, Long> {
    Page<TratamentFitosanitar> findByParcela_Id(Long parcelaId, Pageable pageable);
    List<TratamentFitosanitar> findByParcela_Id(Long parcelaId);

    @Query("SELECT t FROM TratamentFitosanitar t JOIN FETCH t.parcela p WHERE p.id = :parcelaId ORDER BY t.dataEfectuarii DESC")
    List<TratamentFitosanitar> findByParcelaIdWithRelations(@Param("parcelaId") Long parcelaId);

    @Query("SELECT t FROM TratamentFitosanitar t JOIN FETCH t.parcela p JOIN FETCH t.catalogPpp cpp ORDER BY t.dataEfectuarii DESC")
    List<TratamentFitosanitar> findAllWithRelations();
}
