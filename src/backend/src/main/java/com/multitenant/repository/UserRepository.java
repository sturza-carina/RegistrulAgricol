package com.multitenant.repository;

import com.multitenant.model.core.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Page<User> findByTenantId(String tenantId, Pageable pageable);
}

