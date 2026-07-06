package com.multitenant.model.registru;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import java.time.LocalDate;

@Entity
@Table(name = "fertilizari")
@Getter
@Setter
@NoArgsConstructor
@Audited
public class Fertilizare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_aplicarii", nullable = false)
    private LocalDate dataAplicarii;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcela_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Parcela parcela;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_ingrasaminte_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private CatalogIngrasaminte catalogIngrasaminte;

    @Column(name = "cantitate_bruta", nullable = false)
    private Double cantitateBruta;

    @Column(name = "unitate_masura", nullable = false, length = 20)
    private String unitateMasura; // kg/ha or tone/ha

    @Column(name = "aport_azot", nullable = false)
    private Double aportAzot; // kg N / ha

    @Column(name = "aport_fosfor", nullable = false)
    private Double aportFosfor; // kg P / ha

    @Column(name = "aport_potasiu", nullable = false)
    private Double aportPotasiu; // kg K / ha

    @Transient
    public Long getParcelaId() {
        return parcela != null ? parcela.getId() : null;
    }

    @Transient
    public Long getCatalogIngrasaminteId() {
        return catalogIngrasaminte != null ? catalogIngrasaminte.getId() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fertilizare other = (Fertilizare) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
