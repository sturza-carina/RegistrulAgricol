package com.multitenant.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CladireDTO {
    private Long id;
    private String destinatie;
    private Double suprafataConstruita;
    private Integer anTerminare;
    private String materiale;
    private String adresaSauParcela;
    private Long gospodarieId;
    private Long terenId;
}
