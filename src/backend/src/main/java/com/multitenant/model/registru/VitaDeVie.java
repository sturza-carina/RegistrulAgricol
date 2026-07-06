package com.multitenant.model.registru;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vita_de_vie")
@Getter
@Setter
@NoArgsConstructor
public class VitaDeVie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tip_inregistrare", nullable = false, length = 20)
    private TipInregistrareVita tipInregistrare;

    @Column(name = "specie", nullable = false, length = 100)
    private String specie; // Viță de vie

    @Column(name = "soi", length = 100)
    private String soi; // Fetească Regală, Cabernet Sauvignon etc.

    @Column(name = "an_plantare")
    private Integer anPlantare;

    @Column(name = "numar_vite")
    private Integer numarVite; // relevant pt. IZOLAT

    @Column(name = "suprafata_ha")
    private Double suprafataHa; // relevant pt. PLANTATIE

    @Column(name = "densitate_vite_ha")
    private Integer densitateViteHa; // relevant pt. PLANTATIE

    @Column(name = "stare_vita", length = 50)
    private String stareVita; // tânără, pe rod, îmbătrânită

    @Column(name = "sistem_intretinere", length = 100)
    private String sistemIntretinere; // ecologic, convențional

    @Column(name = "sistem_irigare", length = 100)
    private String sistemIrigare;

    @Column(name = "productie_estimata_kg")
    private Double productieEstimataKg;

    @Column(name = "observatii", length = 500)
    private String observatii;

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
        VitaDeVie other = (VitaDeVie) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
