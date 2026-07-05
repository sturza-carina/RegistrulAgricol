package com.multitenant.dto;

import com.multitenant.model.registru.TipFolosintaPasune;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasuneFaneataDTO {

    private Long id;

    @NotNull(message = "Tipul de folosință este obligatoriu")
    private TipFolosintaPasune tipFolosinta;

    @NotNull(message = "Suprafața este obligatorie")
    @Positive(message = "Suprafața trebuie să fie pozitivă")
    private Double suprafataHa;

    private String speciiDominante;

    @Positive(message = "Numărul de animale trebuie să fie pozitiv")
    private Integer numarAnimalePasunat;

    @Positive(message = "Numărul de cosiri trebuie să fie pozitiv")
    private Integer numarCosiriAnuale;

    private Double productieEstimataKgHa;
    private String stareVegetatie;
    private String sistemIntretinere;
    private String sistemIrigare;
    private String observatii;
    private Long parcelaId;
}
