package com.multitenant.repository;

import com.multitenant.model.registru.IstoricMembruGospodarie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IstoricMembruRepository extends JpaRepository<IstoricMembruGospodarie, Long> {
    List<IstoricMembruGospodarie> findByGospodarieIdOrderByDataEvenimentDescIdDesc(Long gospodarieId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE IstoricMembruGospodarie h SET h.document = null WHERE h.document.id = :documentId")
    void nullifyDocumentAssociation(@org.springframework.data.repository.query.Param("documentId") Long documentId);
}
