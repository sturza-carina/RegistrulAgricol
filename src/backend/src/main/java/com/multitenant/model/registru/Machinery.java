package com.multitenant.model.registru;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "utilaje")
@Data
@NoArgsConstructor
public class Machinery {

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

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "gospodarie_id", nullable = false)
    private Gospodarie gospodarie;

    @Transient
    public Long getGospodarieId() {
        return gospodarie != null ? gospodarie.getId() : null;
    }
}
