package com.multitenant.model.registru;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Table(name = "paduri")
@Getter
@Setter
@NoArgsConstructor
@Audited
@SQLDelete(sql = "UPDATE paduri SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public class Padure {
    @jakarta.persistence.Column(nullable = false)
    private boolean deleted = false;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tip_vegetatie", nullable = false, length = 100)
    private String tipVegetatie; // ex: Pădure, Perdea forestieră, Pepinieră, Răchitărie

    @Column(name = "specie_predominanta", length = 100)
    private String speciePredominanta; // ex: Stejar, Fag, Brad

    @Column(name = "suprafata_ha")
    private Double suprafataHa;

    @Column(name = "an_plantare")
    private Integer anPlantare;

    @Column(name = "stare_vegetatie", length = 50)
    private String stareVegetatie; // ex: Tineret, Matur, Degradat

    @Column(name = "observatii", length = 500)
    private String observatii;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parcela_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Parcela parcela;

    @Transient
    public Long getParcelaId() {
        return parcela != null ? parcela.getId() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Padure other = (Padure) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
