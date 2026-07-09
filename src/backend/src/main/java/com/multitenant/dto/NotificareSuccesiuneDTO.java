package com.multitenant.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class NotificareSuccesiuneDTO {
    private Long id;
    private Long defunctId;
    private String defunctNume; // Combinație Nume + Prenume pentru afișare facilă
    private String defunctCnpHash;
    private String numeNotarSpnBin;
    private String numarAdresaOficiala;
    private LocalDate dataTrimitere;
    private String stadiuNotificare; // TRIMIS, IN_LUCRU, FINALIZAT
    private String observatii;
    private String utilizatorOperare;
    private LocalDateTime dataInregistrare;
    
    // Detalii deces aduse pe DTO pentru a marca defunctul
    private LocalDate dataDecesului;
    private String numarCertificatDeces;
}
