package com.multitenant.dto;

import com.multitenant.model.registru.TipInregistrarePom;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PomDTO {

    private Long id;

    @NotNull(message = "Tipul de înregistrare este obligatoriu")
    private TipInregistrarePom tipInregistrare;

    @NotBlank(message = "Specia este obligatorie")
    private String specie;

    private String soi;

    private Integer anPlantare;

    @Positive(message = "Numărul de pomi trebuie să fie pozitiv")
    private Integer numarPomi;

    @Positive(message = "Suprafața trebuie să fie pozitivă")
    private Double suprafataHa;

    @Positive(message = "Densitatea trebuie să fie pozitivă")
    private Integer densitatePomiHa;

    private String starePomi;
    private String sistemIntretinere;
    private String sistemIrigare;
    private Double productieEstimataKg;
    private String observatii;
    private Long parcelaId;
}