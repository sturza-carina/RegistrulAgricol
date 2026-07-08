package com.multitenant.model.registru;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tratamente_fitosanitare")
@Getter
@Setter
@NoArgsConstructor
@Audited
public class TratamentFitosanitar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_efectuarii", nullable = false)
    private LocalDateTime dataEfectuarii;

    @Column(nullable = false, length = 100)
    private String fenofaza; // înfrățire, înspicare, apariția primelor frunze etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcela_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Parcela parcela;

    @Column(name = "agent_daunator", nullable = false, length = 255)
    private String agentDaunator; // boala, dăunătorul sau buruienile vizate

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_ppp_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private CatalogPpp catalogPpp;

    @Column(name = "doza_utilizata", nullable = false)
    private Double dozaUtilizata;

    @Column(name = "suprafata_tratata", nullable = false)
    private Double suprafataTratata; // ha

    @Column(name = "cantitate_totala", nullable = false)
    private Double cantitateTotala; // auto-calculated: suprafata * doza

    @Column(nullable = false, length = 255)
    private String responsabil;

    @Column(name = "semnatura_electronica", columnDefinition = "TEXT")
    private String semnaturaElectronica;

    @Column(name = "data_incepere_recoltare")
    private LocalDate dataIncepereRecoltare; // optional

    @Column(name = "document_dare_consum", length = 100)
    private String documentDareConsum; // optional

    @Column(name = "doza_depasita", nullable = false)
    private Boolean dozaDepasita = false;

    @Column(name = "justificare_supradozaj", columnDefinition = "TEXT")
    private String justificareSupradozaj;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ciclu_productie_id")
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private CicluProductie cicluProductie;

    @Column(name = "unitate_masura_doza", length = 50)
    private String unitateMasuraDoza;

    @Column(name = "data_lansarii")
    private LocalDate dataLansarii;

    @Column(name = "numar_cutii_indivizi")
    private Integer numarCutiiIndivizi;

    @Transient
    public Long getParcelaId() {
        return parcela != null ? parcela.getId() : null;
    }

    @Transient
    public Long getCicluProductieId() {
        return cicluProductie != null ? cicluProductie.getId() : null;
    }

    @Transient
    public Long getCatalogPppId() {
        return catalogPpp != null ? catalogPpp.getId() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TratamentFitosanitar other = (TratamentFitosanitar) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
