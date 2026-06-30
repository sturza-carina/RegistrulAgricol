package com.multitenant.dto;

import com.multitenant.model.common.Adresa;
import com.multitenant.model.registru.TipGospodarie;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GospodarieDTO {
    private Long id;
    private String codGospodarie;
    private Adresa adresa;
    private TipGospodarie tipGospodarie;
    private boolean activa;
    private UatRef uat;

    @Data
    @NoArgsConstructor
    public static class UatRef {
        private Long id;
        private String codSiruta;
        private String denumire;
        private String judet;
    }
}
