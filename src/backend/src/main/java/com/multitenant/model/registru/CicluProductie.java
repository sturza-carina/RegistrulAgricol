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
@Table(name = "cicluri_productie")
@Getter
@Setter
@NoArgsConstructor
@Audited
@SQLDelete(sql = "UPDATE cicluri_productie SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public class CicluProductie {

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

    @Column(nullable = false, length = 100)
    private String cultura;

    @Column(name = "data_infiintare", nullable = false)
    private LocalDate dataInfiintare;

    @Column(name = "data_defisare")
    private LocalDate dataDefisare;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CicluStatus status = CicluStatus.ACTIV;

    @Column(name = "program_sprijin", nullable = false)
    private boolean programSprijin = false;

    @Transient
    public Long getParcelaId() {
        return parcela != null ? parcela.getId() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CicluProductie other = (CicluProductie) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
