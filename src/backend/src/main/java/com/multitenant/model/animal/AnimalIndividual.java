package com.multitenant.model.animal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.multitenant.model.persoana.Persoana;
import com.multitenant.model.registru.Gospodarie;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "animal_individual")
@Data
@NoArgsConstructor
public class AnimalIndividual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gospodarie_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Gospodarie gospodarie;

    // Accept gospodarie by ID on input; READ_ONLY field above handles serialization
    @Transient
    @JsonProperty(value = "gospodarieId", access = JsonProperty.Access.WRITE_ONLY)
    private Long gospodarieId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proprietar_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Persoana proprietar;

    // Accept proprietar by ID on input; READ_ONLY field above handles serialization
    @Transient
    @JsonProperty(value = "proprietarId", access = JsonProperty.Access.WRITE_ONLY)
    private Long proprietarId;

    @Column(name = "numar_crotal")
    private String numarCrotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "specie", nullable = false)
    private SpecieAnimal specie;

    @Column(name = "rasa")
    private String rasa;

    @Enumerated(EnumType.STRING)
    @Column(name = "sex", nullable = false)
    private SexAnimal sex;

    @Column(name = "data_nastere")
    private LocalDate dataNastere;

    @Column(name = "greutate_kg")
    private Double greutateKg;

    @Column(name = "stare_activa", nullable = false)
    private boolean stareActiva = true;

    @Column(name = "tenant_id")
    private String tenantId;

    /**
     * Lista evenimentelor înregistrate pentru acest animal.
     * @JsonIgnore previne recursivitatea infinită la serializare.
     * Folosiți EvenimentAnimalRepository pentru a accesa timeline-ul.
     */
    @OneToMany(mappedBy = "animal", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<EvenimentAnimal> evenimente = new ArrayList<>();


}
