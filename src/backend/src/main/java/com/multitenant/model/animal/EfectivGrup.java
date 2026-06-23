package com.multitenant.model.animal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.multitenant.model.persoana.Persoana;
import com.multitenant.model.registru.Gospodarie;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "efectiv_grup")
@Data
@NoArgsConstructor
public class EfectivGrup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gospodarie_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Gospodarie gospodarie;

    @Transient
    @JsonProperty(value = "gospodarieId", access = JsonProperty.Access.WRITE_ONLY)
    private Long gospodarieId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proprietar_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Persoana proprietar;

    @Transient
    @JsonProperty(value = "proprietarId", access = JsonProperty.Access.WRITE_ONLY)
    private Long proprietarId;

    @Enumerated(EnumType.STRING)
    @Column(name = "specie", nullable = false)
    private SpecieAnimal specie;

    @Column(name = "numar_capete_familii", nullable = false)
    private int numarCapeteFamilii;

    @Column(name = "detalii")
    private String detalii;

    @Column(name = "tenant_id")
    private String tenantId;
}
