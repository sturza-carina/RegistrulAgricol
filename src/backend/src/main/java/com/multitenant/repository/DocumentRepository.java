package com.multitenant.repository;

import com.multitenant.model.registru.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Page<Document> findByGospodarieId(Long gospodarieId, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.gospodarie.uat.codSiruta = :uatCode")
    Page<Document> findByUatCode(@Param("uatCode") String uatCode, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.id = :id AND d.gospodarie.id = :gospodarieId")
    java.util.Optional<Document> findByIdAndGospodarieId(@Param("id") Long id, @Param("gospodarieId") Long gospodarieId);
}