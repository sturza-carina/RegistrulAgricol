package com.multitenant.model.registru;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Cartea Funciara (CF) asociata unui Teren.
 *
 * Relatia este 1-to-1 cu Teren. Instantierea se face AUTOMAT de catre
 * {@link com.multitenant.event.CarteFunciaraEventListener} la crearea unui Teren nou,
 * prin mecanismul de Spring Application Events. Nu trebuie creata manual din controller.
 *
 * Campurile numarCf si numarTopografic sunt nullable intentionat: CF-ul este creat
 * imediat la salvarea terenului, dar numerele se completeaza ulterior de catre operator
 * (dupa sincronizarea cu ANCPI / Cadastru).
 *
 * suprafataTotalaIntabulata este recalculata automat la fiecare adaugare de parcela noua.
 */
@Entity
@Table(name = "carti_funciare")
@Getter
@Setter
@NoArgsConstructor
public class CarteFunciara {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relatia 1-to-1 cu Teren. UNIQUE constraint este in baza de date (V30).
     * Fetch LAZY pentru a evita incarcarea inutila a Teren-ului in contexte
     * in care avem nevoie doar de datele CF-ului.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teren_id", nullable = false, unique = true)
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private Teren teren;

    /**
     * Numarul Cartii Funciare — alocat de ANCPI/Cadastru.
     * Nullable la creare, completat ulterior de operator.
     */
    @Column(name = "numar_cf", length = 100)
    private String numarCf;

    /**
     * Numarul topografic al proprietatii.
     * Nullable la creare, completat ulterior de operator.
     */
    @Column(name = "numar_topografic", length = 100)
    private String numarTopografic;

    /**
     * Suprafata totala intabulata (suma suprafetelor tuturor parcelelor asociate terenului).
     * Recalculata automat de listener la fiecare adaugare de parcela.
     */
    @Column(name = "suprafata_totala_intabulata")
    private Double suprafataTotalaIntabulata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Helper ---

    @Transient
    public Long getTerenId() {
        return teren != null ? teren.getId() : null;
    }

    // --- Identity (pattern standard conform Docs/Bugs/Lombok_JPA_Entities_Refactoring.md) ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarteFunciara other = (CarteFunciara) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
