package com.multitenant.model.registru;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pomi")
@Getter
@Setter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE pomi SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public class Pom {
    @jakarta.persistence.Column(nullable = false)
    private boolean deleted = false;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tip_inregistrare", nullable = false, length = 20)
    private TipInregistrarePom tipInregistrare;

    @Column(name = "specie", nullable = false, length = 100)
    private String specie; // mÄƒr, pÄƒr, prun, nuc etc.

    @Column(name = "soi", length = 100)
    private String soi;

    @Column(name = "an_plantare")
    private Integer anPlantare;

    @Column(name = "numar_pomi")
    private Integer numarPomi; // relevant pt. IZOLAT

    @Column(name = "suprafata_ha")
    private Double suprafataHa; // relevant pt. PLANTATIE

    @Column(name = "densitate_pomi_ha")
    private Integer densitatePomiHa; // relevant pt. PLANTATIE

    @Column(name = "stare_pomi", length = 50)
    private String starePomi; // tÃ¢nÄƒr, pe rod, Ã®mbÄƒtrÃ¢nit

    @Column(name = "sistem_intretinere", length = 100)
    private String sistemIntretinere; // ecologic, convenÈ›ional

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
        Pom other = (Pom) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
