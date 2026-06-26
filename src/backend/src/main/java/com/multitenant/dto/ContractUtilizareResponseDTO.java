package com.multitenant.dto;

import com.multitenant.model.registru.StatusContractUtilizare;
import com.multitenant.model.registru.TipContractUtilizare;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ContractUtilizareResponseDTO {
    private Long id;
    private TerenRef teren;
    private PersoanaRef locatorProprietar;
    private PersoanaRef locatorUtilizator;
    private PersoanaRef utilizatorOperare;
    
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
    private boolean esteActiv;

    @Data
    @NoArgsConstructor
    public static class TerenRef {
        private Long id;
        private String denumire;
        public TerenRef(Long id, String denumire) {
            this.id = id;
            this.denumire = denumire;
        }
    }

    @Data
    @NoArgsConstructor
    public static class PersoanaRef {
        private Long id;
        public PersoanaRef(Long id) {
            this.id = id;
        }
    }
}
