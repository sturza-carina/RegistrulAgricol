package com.multitenant.dto;

import lombok.Data;

/**
 * DTO pentru actualizarea manuala a datelor unei CarteFunciara de catre operator.
 * Folosit de endpoint-ul PUT /api/carti-funciare/{id}.
 *
 * Campurile sunt optional nullable: operatorul poate completa doar numarCf,
 * doar numarTopografic, sau ambele intr-o singura cerere.
 */
@Data
public class CarteFunciaraDTO {

    /**
     * Numarul Cartii Funciare alocat de ANCPI.
     * Ex: "12345" sau "CF-12345-UAT"
     */
    private String numarCf;

    /**
     * Numarul topografic al proprietatii.
     * Ex: "Top/123/2" sau "1234/2/a"
     */
    private String numarTopografic;
}
