package com.multitenant.dto;

import com.multitenant.model.animal.TipEvenimentAnimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class EvenimentAnimalDTO {
    private Long id;
    private AnimalIndividualDTO animal;
    private TipEvenimentAnimal tipEveniment;
    private LocalDate dataEveniment;
    private String detalii;
    private String destinatarTenantId;
    private String tenantId;
}
