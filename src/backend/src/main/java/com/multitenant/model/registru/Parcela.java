package com.multitenant.model.registru;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;

@Entity
@Table(name = "parcele")
@Data
@NoArgsConstructor
public class Parcela {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String denumire;

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
}
