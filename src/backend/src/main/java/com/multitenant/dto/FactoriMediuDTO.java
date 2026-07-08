package com.multitenant.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FactoriMediuDTO {
    private Long id;
    private Long parcelaId;
    private String parcelaDenumire;
    private Double temperatura;
    private Double umiditateRelativa;
    private LocalDateTime dataInregistrare;
}
