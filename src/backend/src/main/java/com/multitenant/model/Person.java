package com.multitenant.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "persons")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "person_type", discriminatorType = DiscriminatorType.STRING)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "personType"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PhysicalPerson.class, name = "PHYSICAL_PERSON"),
    @JsonSubTypes.Type(value = LegalEntity.class, name = "LEGAL_ENTITY")
})
@Data
@NoArgsConstructor
public abstract class Person {

    @Column(name = "person_type", insertable = false, updatable = false)
    private String personType;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Address (Domiciliu / Sediu)
    @Embedded
    private Address address;

    // Contact Information
    @Column(name = "phone_number")
    private String phoneNumber;
    
    private String email;

    // Agriculture Register specific (Registrul Agricol)
    @Column(name = "register_volume")
    private String registerVolume;

    @Column(name = "register_position")
    private String registerPosition;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Optional relation for tracking multi-tenancy.
    @Column(name = "tenant_id")
    private String tenantId;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PersonRelation> relations = new ArrayList<>();
}
