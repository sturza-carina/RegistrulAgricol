package com.multitenant.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class FertilizareDTO {
    private Long id;
    private LocalDate dataAplicarii;
    private Long parcelaId;
    private String parcelaDenumire;
    private Long catalogIngrasaminteId;
    private String catalogIngrasaminteDenumire;
    private String catalogIngrasaminteTip; // Organic / Chimic
    private Double procentAzot;
    private Double procentFosfor;
    private Double procentPotasiu;
    private Double cantitateBruta;
    private String unitateMasura; // kg/ha or tone/ha
    private Double aportAzot;
    private Double aportFosfor;
    private Double aportPotasiu;
}
