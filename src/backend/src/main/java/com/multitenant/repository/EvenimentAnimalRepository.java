package com.multitenant.repository;

import com.multitenant.model.animal.EvenimentAnimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EvenimentAnimalRepository extends JpaRepository<EvenimentAnimal, Long> {

    /**
     * Returnează toate evenimentele unui animal, ordonate descrescător după dată
     * (cel mai recent eveniment primul — util pentru timeline).
     */
    List<EvenimentAnimal> findByAnimalIdOrderByDataEvenimentDesc(Long animalId);
}
