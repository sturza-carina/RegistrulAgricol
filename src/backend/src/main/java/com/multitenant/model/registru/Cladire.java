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
@Table(name = "cladiri")
@Getter
@Setter
@NoArgsConstructor
@Audited
@SQLDelete(sql = "UPDATE cladiri SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public class Cladire {
    @jakarta.persistence.Column(nullable = false)
    private boolean deleted = false;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String destinatie;

    @Column(name = "suprafata_construita", nullable = false)
    private Double suprafataConstruita;

    @Column(name = "an_terminare")
    private Integer anTerminare;

    @Column(length = 255)
    private String materiale;

    @Column(name = "adresa_sau_parcela", length = 255)
    private String adresaSauParcela;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gospodarie_id", nullable = false)
    private Gospodarie gospodarie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teren_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Teren teren;

    @Transient
    public Long getGospodarieId() {
        return gospodarie != null ? gospodarie.getId() : null;
    }

    @Transient
    public Long getTerenId() {
        return teren != null ? teren.getId() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cladire other = (Cladire) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
