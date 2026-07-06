package com.multitenant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticaCulturaDto {
    private String specieCultura;
    private Double suprafataTotalaHa;
    private Double productieTotalaTone;
}
