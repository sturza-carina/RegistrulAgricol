package com.multitenant.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import com.multitenant.model.registru.TipSursaApa;

@Data
@NoArgsConstructor
public class SursaApaDTO {

    private Long id;
    private Long parcelaId;
    private TipSursaApa tipSursa;
    private Double debitMcOra;
    private boolean stareFunctionare;
}
