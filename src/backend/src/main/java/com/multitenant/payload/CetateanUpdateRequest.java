package com.multitenant.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CetateanUpdateRequest {
    @NotBlank
    private String nume;

    @NotBlank
    private String prenume;

    @NotBlank
    private String telefon;

    @NotBlank
    private String judet;

    @NotBlank
    private String localitate;

    @NotBlank
    private String strada;

    @NotBlank
    private String numar;

    private String bloc;
    private String scara;
    private String etaj;
    private String apartament;
}
