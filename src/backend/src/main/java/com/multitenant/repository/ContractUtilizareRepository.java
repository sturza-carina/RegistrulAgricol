package com.multitenant.repository;

import com.multitenant.model.registru.ContractUtilizare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractUtilizareRepository extends JpaRepository<ContractUtilizare, Long> {
    List<ContractUtilizare> findByTerenId(Long terenId);
}
