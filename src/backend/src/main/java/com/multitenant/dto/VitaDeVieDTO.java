package com.multitenant.dto;

import com.multitenant.model.registru.TipInregistrareVita;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VitaDeVieDTO {

    private Long id;

    @NotNull(message = "Tipul de înregistrare este obligatoriu")
    private TipInregistrareVita tipInregistrare;

    @NotBlank(message = "Specia este obligatorie")
    private String specie;

    private String soi;

    private Integer anPlantare;

    @Positive(message = "Numărul de vițe trebuie să fie pozitiv")
    private Integer numarVite;

    @Positive(message = "Suprafața trebuie să fie pozitivă")
    private Double suprafataHa;

    @Positive(message = "Densitatea trebuie să fie pozitivă")
    private Integer densitateViteHa;

    private String stareVita;
    private String sistemIntretinere;
    private String sistemIrigare;
    private Double productieEstimataKg;
    private String observatii;
    private Long parcelaId;
}
