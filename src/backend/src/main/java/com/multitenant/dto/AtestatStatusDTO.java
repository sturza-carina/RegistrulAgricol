package com.multitenant.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AtestatStatusDTO {
    private boolean areAtestat;
    private String numarAtestat;
    private LocalDate atestatValabilPanaLa;

    private boolean areCarnet;
    private String numarCarnet;
    private LocalDate carnetDataEliberare;
    
    private String stareCerereAtestat;
    private String stareCerereCarnet;
}