package com.multitenant.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PadureDTO {
    private Long id;

    @NotBlank(message = "Tipul de vegetație este obligatoriu")
    private String tipVegetatie;

    private String speciePredominanta;

    @NotNull(message = "Suprafața este obligatorie")
    @Positive(message = "Suprafața trebuie să fie pozitivă")
    private Double suprafataHa;

    private Integer anPlantare;
    private String stareVegetatie;
    private String observatii;
    private Long parcelaId;
}
