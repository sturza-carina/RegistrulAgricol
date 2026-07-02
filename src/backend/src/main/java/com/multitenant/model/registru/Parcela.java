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
        if (teren != null && teren.getGospodarie() != null) {
            return "Gospodăria " + teren.getGospodarie().getCodGospodarie();
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
