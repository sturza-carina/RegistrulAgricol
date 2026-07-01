package com.multitenant.model.animal;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Reprezintă un eveniment din istoricul (timeline-ul) unui animal individual.
 * Fiecare eveniment este imutabil după creare — nu se editează, doar se adaugă.
 */
@Entity
@Table(name = "evenimente_animale")
@Getter
@Setter
@NoArgsConstructor
public class EvenimentAnimal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Animalul căruia îi aparține acest eveniment.
     * READ_ONLY la serializare; scris prin evenimentAnimalId în service.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private AnimalIndividual animal;

    @Enumerated(EnumType.STRING)
    @Column(name = "tip_eveniment", nullable = false)
    private TipEvenimentAnimal tipEveniment;

    @Column(name = "data_eveniment", nullable = false)
    private LocalDate dataEveniment;

    /**
     * Câmp opțional pentru observații: număr factură, motiv deces, UAT destinație etc.
     */
    @Column(name = "detalii", columnDefinition = "TEXT")
    private String detalii;

    /**
     * Pentru evenimentele de tip VANZARE care declansă un transfer inter-tenant:
     * ID-ul tenant-ului de destinație. Permite CrossTenantTransferService să śtie
     * către ce schemă PostgreSQL să copieze animalul și istoricul său.
     * Rămâne NULL pentru orice alt tip de eveniment.
     */
    @Column(name = "destinatar_tenant_id")
    private String destinatarTenantId;

    /**
     * Tenant-ul căruia îi aparține înregistrarea (pentru arhitectura multi-tenant).
     */
    @Column(name = "tenant_id")
    private String tenantId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvenimentAnimal other = (EvenimentAnimal) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
