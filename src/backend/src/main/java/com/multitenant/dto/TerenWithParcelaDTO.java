package com.multitenant.dto;

import com.multitenant.model.registru.Teren;
import com.multitenant.model.registru.Parcela;
import lombok.Data;

@Data
public class TerenWithParcelaDTO {
    private Teren teren;
    private Parcela parcela;
}
