package com.multitenant.model.registru;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "recoltari")
@Getter
@Setter
@NoArgsConstructor
@Audited
@SQLDelete(sql = "UPDATE recoltari SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public class Recoltare {

    @Column(nullable = false)
    private boolean deleted = false;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parcela_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Parcela parcela;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ciclu_productie_id")
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private CicluProductie cicluProductie;

    @Column(nullable = false, length = 100)
    private String cultura;

    @Column(name = "data_recoltare", nullable = false)
    private LocalDate dataRecoltare;

    @Column(name = "cantitate_kg", nullable = false)
    private Double cantitateKg;

    @Transient
    public Long getParcelaId() {
        return parcela != null ? parcela.getId() : null;
    }

    @Transient
    public Long getCicluProductieId() {
        return cicluProductie != null ? cicluProductie.getId() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recoltare other = (Recoltare) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
