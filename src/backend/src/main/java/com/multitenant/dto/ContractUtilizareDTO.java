package com.multitenant.dto;

import com.multitenant.model.registru.StatusContractUtilizare;
import com.multitenant.model.registru.TipContractUtilizare;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ContractUtilizareDTO {
    private Long terenId;
    private Long locatorProprietarId;
    private Long locatorUtilizatorId;
    private Long utilizatorOperareId;
    private TipContractUtilizare tipContract;
    private String numarContract;
    private LocalDate dataSemnare;
    private LocalDate dataInceput;
    private LocalDate dataSfarsit;
    private Double pretArendaRonAn;
    private Double pretArendaGrauKgHa;
    private boolean indexarePret;
    private StatusContractUtilizare statusContract;
    private String motivIncetare;
    private LocalDate dataOperare;
    private boolean esteActiv = true;
}
