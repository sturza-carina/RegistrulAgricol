package com.multitenant.dto;

import com.multitenant.model.registru.TipEvenimentMembru;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class IstoricMembruDTO {
    private Long id;
    private Long gospodarieId;
    private Long persoanaId;
    private String numeCompletPersoana; // For frontend presentation
    private TipEvenimentMembru tipEveniment;
    private LocalDate dataEveniment;
    private String observatii;
    private LocalDateTime createdAt;
}
