package com.multitenant.repository.core;

import com.multitenant.model.core.Cetatean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CetateanRepository extends JpaRepository<Cetatean, Long> {
    Optional<Cetatean> findByEmail(String email);
    Optional<Cetatean> findByCnp(String cnp);
}
