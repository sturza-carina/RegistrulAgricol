package com.multitenant.model.registru;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pasuni_fanete")
@Getter
@Setter
@NoArgsConstructor
public class PasuneFaneata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tip_folosinta", nullable = false, length = 20)
    private TipFolosintaPasune tipFolosinta;

    @Column(name = "suprafata_ha", nullable = false)
    private Double suprafataHa;

    @Column(name = "specii_dominante", length = 255)
    private String speciiDominante; // ex: păiuș, trifoi, iarbă de câmp

    @Column(name = "numar_animale_pasunat")
    private Integer numarAnimalePasunat; // relevant pt. PASUNAT

    @Column(name = "numar_cosiri_anuale")
    private Integer numarCosiriAnuale; // relevant pt. COSIT

    @Column(name = "productie_estimata_kg_ha")
    private Double productieEstimataKgHa; // masă verde sau fân, kg/ha

    @Column(name = "stare_vegetatie", length = 50)
    private String stareVegetatie; // bună, degradată, în regenerare

    @Column(name = "sistem_intretinere", length = 100)
    private String sistemIntretinere; // ecologic, convențional

    @Column(name = "sistem_irigare", length = 100)
    private String sistemIrigare;

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
        PasuneFaneata other = (PasuneFaneata) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
