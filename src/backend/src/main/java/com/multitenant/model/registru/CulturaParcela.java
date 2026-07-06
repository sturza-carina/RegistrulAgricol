package com.multitenant.model.registru;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "culturi_parcele")
@Getter
@Setter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE culturi_parcele SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public class CulturaParcela {
    @jakarta.persistence.Column(nullable = false)
    private boolean deleted = false;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "an_agricol", nullable = false)
    private Integer anAgricol;

    @Column(name = "specie_cultura", nullable = false, length = 100)
    private String specieCultura;

    @Column(name = "suprafata_cultivata_ha", nullable = false)
    private Double suprafataCultivataHa;

    @Column(name = "data_insamantare")
    private LocalDate dataInsamantare;

    @Column(name = "data_recoltare")
    private LocalDate dataRecoltare;

    @Column(name = "productie_hectar_kg")
    private Double productieHectarKg;

    @Column(name = "productie_totala_tone")
    private Double productieTotalaTone;

    @Column(name = "sistem_irigare", length = 100)
    private String sistemIrigare;

    @Column(name = "tip_sol", length = 100)
    private String tipSol;

    @Column(name = "cultura_precedenta", length = 100)
    private String culturaPrecedenta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parcela_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private Parcela parcela;

    @Transient
    public Long getParcelaId() {
        return parcela != null ? parcela.getId() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CulturaParcela other = (CulturaParcela) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
