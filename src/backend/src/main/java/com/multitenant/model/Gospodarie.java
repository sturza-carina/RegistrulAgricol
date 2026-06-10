package com.multitenant.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gospodarie")
@Data
@NoArgsConstructor
public class Gospodarie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cod_gospodarie", nullable = false, length = 100)
    private String codGospodarie;

    @Embedded
    private Adresa adresa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tip_gospodarie", nullable = false, length = 50)
    private TipGospodarie tipGospodarie;

    @Column(nullable = false)
    private boolean activa = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uat_id")
    private Uat uat;
}
