package com.multitenant.model.registru;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "catalog_ppp")
@Getter
@Setter
@NoArgsConstructor
@Audited
public class CatalogPpp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "denumire_comerciala", nullable = false, length = 255)
    private String denumireComerciala;

    @Column(nullable = false, length = 100)
    private String tip; // Fungicid, Erbicid, Insecticid

    @Column(name = "daunator_vizat", length = 255)
    private String daunatorVizat;

    @Column(name = "doza_omologata", nullable = false)
    private Double dozaOmologata; // L/ha or kg/ha

    @Column(name = "timp_pauza", nullable = false)
    private Integer timpPauza; // days

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CatalogPpp other = (CatalogPpp) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
