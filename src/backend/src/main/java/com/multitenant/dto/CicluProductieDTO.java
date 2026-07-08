package com.multitenant.dto;

import com.multitenant.model.registru.CicluStatus;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CicluProductieDTO {
    private Long id;
    private Long parcelaId;
    private String parcelaDenumire;
    private String cultura;
    private LocalDate dataInfiintare;
    private LocalDate dataDefisare;
    private CicluStatus status;
    private boolean programSprijin;
    private String warning;
}
