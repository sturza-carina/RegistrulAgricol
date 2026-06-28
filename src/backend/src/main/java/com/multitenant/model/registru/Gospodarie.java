package com.multitenant.model.registru;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.multitenant.model.common.Adresa;
import com.multitenant.model.core.Uat;

@Entity
@Table(name = "gospodarii")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uat_id")
    private Uat uat;
}
