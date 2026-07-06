package com.multitenant.model.registru;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "utilaje")
@Getter
@Setter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE utilaje SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public class Machinery {
    @jakarta.persistence.Column(nullable = false)
    private boolean deleted = false;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tip_utilaj", nullable = false, length = 50)
    private String tipUtilaj;

    @Column(nullable = false, length = 100)
    private String marca;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(name = "an_fabricatie")
    private Integer anFabricatie;

    @Column(name = "numar_inmatriculare", length = 50)
    private String numarInmatriculare;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gospodarie_id", nullable = false)
    private Gospodarie gospodarie;

    @Transient
    public Long getGospodarieId() {
        return gospodarie != null ? gospodarie.getId() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Machinery other = (Machinery) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
