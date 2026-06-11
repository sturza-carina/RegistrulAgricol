package com.multitenant.model.registru;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Polygon;

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

    @Column(nullable = false, columnDefinition = "geometry(Polygon, 4326)")
    private Polygon polygon;

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
