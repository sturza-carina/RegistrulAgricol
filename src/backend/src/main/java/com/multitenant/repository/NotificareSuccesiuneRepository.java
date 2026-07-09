package com.multitenant.repository;

import com.multitenant.model.registru.NotificareSuccesiune;
import com.multitenant.model.persoana.PersoanaFizica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificareSuccesiuneRepository extends JpaRepository<NotificareSuccesiune, Long> {

    @Query("SELECT n FROM NotificareSuccesiune n WHERE n.defunctCnpHash = :cnpHash")
    List<NotificareSuccesiune> findByDefunctCnpHash(@Param("cnpHash") String cnpHash);

    default List<NotificareSuccesiune> findByDefunctCnpClar(String cnpClar) {
        if (cnpClar == null) {
            return List.of();
        }
        String hash = PersoanaFizica.generateBlindIndex(cnpClar);
        return findByDefunctCnpHash(hash);
    }
}
