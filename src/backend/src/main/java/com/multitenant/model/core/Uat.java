package com.multitenant.model.core;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "uat")
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Uat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cod_siruta", nullable = false, length = 50)
    private String codSiruta;

    @Column(nullable = false, length = 255)
    private String denumire;

    @Column(nullable = false, length = 100)
    private String judet;

    @Column(name = "tip_uat", nullable = false, length = 50)
    private String tipUat;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
