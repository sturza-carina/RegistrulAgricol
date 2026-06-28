package com.multitenant.dto;

import com.multitenant.model.animal.SexAnimal;
import com.multitenant.model.animal.SpecieAnimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class AnimalIndividualDTO {
    private Long id;
    private GospodarieDTO gospodarie;
    private Long gospodarieId;
    private PersoanaDTO proprietar;
    private Long proprietarId;
    private String numarCrotal;
    private SpecieAnimal specie;
    private String rasa;
    private SexAnimal sex;
    private LocalDate dataNastere;
    private Double greutateKg;
    private boolean stareActiva;
    private String tenantId;
}
