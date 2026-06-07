package com.multitenant.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "persons")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "person_type", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor
public abstract class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Address (Domiciliu / Sediu)
    private String county;
    private String city;
    private String village;
    private String street;
    @Column(name = "street_number")
    private String streetNumber;
    private String block;
    private String staircase;
    private String floor;
    private String apartment;
    @Column(name = "postal_code")
    private String postalCode;

    // Contact Information
    @Column(name = "phone_number")
    private String phoneNumber;
    
    private String email;

    // Agriculture Register specific (Registrul Agricol)
    @Column(name = "register_volume")
    private String registerVolume;

    @Column(name = "register_position")
    private String registerPosition;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Optional relation for tracking multi-tenancy.
    @Column(name = "tenant_id")
    private String tenantId;
}
