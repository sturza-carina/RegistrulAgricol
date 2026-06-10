package com.multitenant.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
public class Adresa {

    private String county;
    private String localitate;
    private String street;

    @Column(name = "street_number")
    private String streetNumber;

    @Column(nullable = true)
    private String building;
    
    @Column(nullable = true)
    private String staircase;
    
    @Column(nullable = true)
    private Integer floor;
    
    @Column(name = "apartment_number", nullable = true)
    private Integer apartmentNumber;

    @Column(name = "postal_code")
    private String postalCode;
}


