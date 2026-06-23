package com.multitenant.repository;

import com.multitenant.model.registru.Machinery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MachineryRepository extends JpaRepository<Machinery, Long> {
    @Query("SELECT m FROM Machinery m WHERE m.gospodarie.id = :gospodarieId")
    List<Machinery> findByGospodarieId(@Param("gospodarieId") Long gospodarieId);
}
