package com.multitenant.repository;

import com.multitenant.model.registru.IstoricMembruGospodarie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IstoricMembruRepository extends JpaRepository<IstoricMembruGospodarie, Long> {
    List<IstoricMembruGospodarie> findByGospodarieIdOrderByDataEvenimentDescIdDesc(Long gospodarieId);
}
