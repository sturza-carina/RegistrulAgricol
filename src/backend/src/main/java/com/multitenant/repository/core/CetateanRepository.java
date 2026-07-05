package com.multitenant.repository.core;

import com.multitenant.model.core.Cetatean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CetateanRepository extends JpaRepository<Cetatean, Long> {
    Optional<Cetatean> findByEmail(String email);
    Optional<Cetatean> findByCnpHash(String cnpHash);

    default Optional<Cetatean> findByCnpClar(String cnp) {
        if (cnp == null || cnp.trim().isEmpty()) {
            return Optional.empty();
        }
        return findByCnpHash(com.multitenant.util.CryptoUtils.hashSha256(cnp.trim()));
    }

    default Optional<Cetatean> findByCnp(String cnp) {
        return findByCnpClar(cnp);
    }
}
