package com.multitenant.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RecoltareDTO {
    private Long id;
    private Long parcelaId;
    private String parcelaDenumire;
    private Long cicluProductieId;
    private String cicluProductieCultura;
    private String cultura;
    private LocalDate dataRecoltare;
    private Double cantitateKg;
}
