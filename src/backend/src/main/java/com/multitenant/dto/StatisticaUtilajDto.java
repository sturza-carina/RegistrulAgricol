package com.multitenant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticaUtilajDto {
    private String tipUtilaj;
    private Long totalUnitati;
}
