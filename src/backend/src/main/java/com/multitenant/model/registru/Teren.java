package com.multitenant.model.registru;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "terenuri")
@Getter
@Setter
@NoArgsConstructor
public class Teren {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Denumirea terenului este obligatorie.")
    @Column(nullable = false, length = 255)
    private String denumire;

    @Column(name = "tip_teren", length = 100)
    private String tipTeren;

    @Column(name = "stereo70_coordinates", columnDefinition = "TEXT")
    private String stereo70Coordinates;

    @Column(columnDefinition = "jsonb")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private com.fasterxml.jackson.databind.JsonNode polygon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gospodarie_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private Gospodarie gospodarie;
    @Transient
    public Long getGospodarieId() {
        return gospodarie != null ? gospodarie.getId() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Teren other = (Teren) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
