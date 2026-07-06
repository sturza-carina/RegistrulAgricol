package com.multitenant.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CetateanLoginRequest {
    @NotBlank
    private String email;
    
    @NotBlank
    private String parola;
}
