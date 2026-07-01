package com.multitenant.model.persoana;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import com.multitenant.model.common.Adresa;
import com.multitenant.model.registru.Gospodarie;
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
    @JsonSubTypes.Type(value = PersoanaFizica.class, name = "PHYSICAL_PERSON"),
    @JsonSubTypes.Type(value = PersoanaJuridica.class, name = "LEGAL_ENTITY")
})
@Getter
@Setter
@NoArgsConstructor
public abstract class Persoana {

    @Column(name = "person_type", insertable = false, updatable = false)
    private String personType;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Adresa (Domiciliu / Sediu)
    @Embedded
    private Adresa adresa;

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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "persoana", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RelatieRudenie> relations = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "persoane_gospodarii",
        joinColumns = @JoinColumn(name = "persoana_id"),
        inverseJoinColumns = @JoinColumn(name = "gospodarie_id")
    )
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<Gospodarie> gospodarii = new ArrayList<>();

    @Transient
    public List<Long> getGospodarieIds() {
        if (gospodarii == null) return new ArrayList<>();
        return gospodarii.stream().map(Gospodarie::getId).toList();
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    public void setGospodarieIds(List<Long> ids) {
        if (ids != null) {
            this.gospodarii = new ArrayList<>();
            for (Long id : ids) {
                Gospodarie g = new Gospodarie();
                g.setId(id);
                this.gospodarii.add(g);
            }
        } else {
            this.gospodarii = new ArrayList<>();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Persoana other = (Persoana) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}


