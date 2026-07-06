package com.multitenant.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CetateanSignupRequest {
    @NotBlank
    private String nume;
    
    private String prenume;

    private String tipPersoana;
    
    @NotBlank
    private String cnp;
    
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    private String parola;
    
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
