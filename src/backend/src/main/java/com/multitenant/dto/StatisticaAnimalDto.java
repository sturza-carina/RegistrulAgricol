package com.multitenant.dto;

import com.multitenant.model.animal.SpecieAnimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticaAnimalDto {
    private SpecieAnimal specie;
    private Long totalCapete;
    private Long masculi;
    private Long femele;
}
