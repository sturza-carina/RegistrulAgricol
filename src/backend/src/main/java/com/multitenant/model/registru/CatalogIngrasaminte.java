package com.multitenant.model.registru;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "catalog_ingrasaminte")
@Getter
@Setter
@NoArgsConstructor
@Audited
public class CatalogIngrasaminte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String denumire;

    @Column(nullable = false, length = 50)
    private String tip; // Organic / Chimic

    @Column(name = "procent_azot", nullable = false)
    private Double procentAzot = 0.0;

    @Column(name = "procent_fosfor", nullable = false)
    private Double procentFosfor = 0.0;

    @Column(name = "procent_potasiu", nullable = false)
    private Double procentPotasiu = 0.0;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CatalogIngrasaminte other = (CatalogIngrasaminte) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
