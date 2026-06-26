package com.multitenant.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SursaApaDTO {

    private Long id;
    private Long parcelaId;
    private String tipSursa;
    private Double debitMcOra;
    private boolean stareFunctionare;
}
