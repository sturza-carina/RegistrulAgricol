package com.multitenant.model.registru;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pasuni_fanete")
@Getter
@Setter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE pasuni_fanete SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public class PasuneFaneata {
    @jakarta.persistence.Column(nullable = false)
    private boolean deleted = false;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tip_folosinta", nullable = false, length = 20)
    private TipFolosintaPasune tipFolosinta;

    @Column(name = "suprafata_ha", nullable = false)
    private Double suprafataHa;

    @Column(name = "specii_dominante", length = 255)
    private String speciiDominante; // ex: pÄƒiuÈ™, trifoi, iarbÄƒ de cÃ¢mp

    @Column(name = "numar_animale_pasunat")
    private Integer numarAnimalePasunat; // relevant pt. PASUNAT

    @Column(name = "numar_cosiri_anuale")
    private Integer numarCosiriAnuale; // relevant pt. COSIT

    @Column(name = "productie_estimata_kg_ha")
    private Double productieEstimataKgHa; // masÄƒ verde sau fÃ¢n, kg/ha

    @Column(name = "stare_vegetatie", length = 50)
    private String stareVegetatie; // bunÄƒ, degradatÄƒ, Ã®n regenerare

    @Column(name = "sistem_intretinere", length = 100)
    private String sistemIntretinere; // ecologic, convenÈ›ional

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
