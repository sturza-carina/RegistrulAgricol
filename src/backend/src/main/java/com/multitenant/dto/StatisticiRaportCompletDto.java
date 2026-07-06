package com.multitenant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticiRaportCompletDto {
    private List<StatisticaCulturaDto> culturi;
    private List<StatisticaCategorieFolosintaDto> categoriiFolosinta;
    private List<StatisticaAnimalDto> animaleIndividuale;
    private List<StatisticaEfectivGrupDto> efectiveGrup;
    private List<StatisticaUtilajDto> utilaje;
}
