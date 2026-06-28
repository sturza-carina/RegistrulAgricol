package com.multitenant.dto;

import com.multitenant.model.animal.SpecieAnimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class EfectivGrupDTO {
    private Long id;
    private GospodarieDTO gospodarie;
    private Long gospodarieId;
    private PersoanaDTO proprietar;
    private Long proprietarId;
    private SpecieAnimal specie;
    private Integer numarCapeteFamilii;
    private LocalDate dataInregistrare;
    private String detalii;
    private String tenantId;
}
