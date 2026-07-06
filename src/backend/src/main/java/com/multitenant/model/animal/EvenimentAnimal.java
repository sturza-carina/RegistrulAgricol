package com.multitenant.model.animal;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * ReprezintÄƒ un eveniment din istoricul (timeline-ul) unui animal individual.
 * Fiecare eveniment este imutabil dupÄƒ creare â€” nu se editeazÄƒ, doar se adaugÄƒ.
 */
@Entity
@Table(name = "evenimente_animale")
@Getter
@Setter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE evenimente_animale SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public class EvenimentAnimal {
    @jakarta.persistence.Column(nullable = false)
    private boolean deleted = false;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Animalul cÄƒruia Ã®i aparÈ›ine acest eveniment.
     * READ_ONLY la serializare; scris prin evenimentAnimalId Ã®n service.
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
     * CÃ¢mp opÈ›ional pentru observaÈ›ii: numÄƒr facturÄƒ, motiv deces, UAT destinaÈ›ie etc.
     */
    @Column(name = "detalii", columnDefinition = "TEXT")
    private String detalii;

    /**
     * Pentru evenimentele de tip VANZARE care declansÄƒ un transfer inter-tenant:
     * ID-ul tenant-ului de destinaÈ›ie. Permite CrossTenantTransferService sÄƒ Å›tie
     * cÄƒtre ce schemÄƒ PostgreSQL sÄƒ copieze animalul È™i istoricul sÄƒu.
     * RÄƒmÃ¢ne NULL pentru orice alt tip de eveniment.
     */
    @Column(name = "destinatar_tenant_id")
    private String destinatarTenantId;

    /**
     * Tenant-ul cÄƒruia Ã®i aparÈ›ine Ã®nregistrarea (pentru arhitectura multi-tenant).
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
