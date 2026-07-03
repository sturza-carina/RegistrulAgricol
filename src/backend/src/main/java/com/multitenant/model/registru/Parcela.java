package com.multitenant.model.registru;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Table(name = "parcele")
@Getter
@Setter
@NoArgsConstructor
@Audited
public class Parcela {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Denumirea parcelei este obligatorie.")
    @Column(nullable = false, length = 255)
    private String denumire;

    @NotNull(message = "Suprafata este obligatorie.")
    @Positive(message = "Suprafata trebuie sa fie pozitiva.")
    @Column(nullable = false)
    private Double suprafata;

    @Column(name = "categorie_folosinta", length = 100)
    private String categorieFolosinta;

    /**
     * Numarul cadastral al parcelei — alocat de ANCPI/Cadastru.
     * Cheie pentru sincronizarea cu RAN si pentru declaratiile APIA.
     */
    @Column(name = "numar_cadastral", length = 100)
    private String numarCadastral;

    /**
     * Tipul de zona: INTRAVILAN sau EXTRAVILAN.
     * Critic pentru calculul impozitului pe teren (Codul Fiscal, art. 463-467)
     * si pentru clasificarea bunului in CarteFunciara.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tip_zona", length = 20)
    private TipZona tipZona;

    /**
     * Titularul dreptului de folosinta al parcelei.
     * Poate fi numele unui proprietar, al unui arendas sau al unui concesionar.
     * Camp text liber pentru flexibilitate; relatia formala se face prin ContractUtilizare.
     */
    @Column(name = "titular_drept_folosinta", length = 255)
    private String titularDreptFolosinta;

    @Column(columnDefinition = "jsonb")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private JsonNode polygon;

    @Transient
    private String stereo70Coordinates;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teren_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Teren teren;

    @Transient
    public Long getTerenId() {
        return teren != null ? teren.getId() : null;
    }

    @Transient
    public String getGospodarieName() {
        if (teren != null && org.hibernate.Hibernate.isInitialized(teren)) {
            Teren t = teren;
            if (t.getGospodarie() != null && org.hibernate.Hibernate.isInitialized(t.getGospodarie())) {
                return "Gospodăria " + t.getGospodarie().getCodGospodarie();
            }
        }
        return "Gospodărie Necunoscută";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parcela other = (Parcela) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
