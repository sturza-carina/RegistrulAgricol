package com.multitenant.model.animal;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.multitenant.model.persoana.Persoana;
import com.multitenant.model.registru.Gospodarie;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "efective_grup")
@Getter
@Setter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE efective_grup SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public class EfectivGrup {
    @jakarta.persistence.Column(nullable = false)
    private boolean deleted = false;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gospodarie_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Gospodarie gospodarie;

    @Transient
    @JsonProperty(value = "gospodarieId", access = JsonProperty.Access.WRITE_ONLY)
    private Long gospodarieId;

    @ManyToOne(fetch = FetchType.LAZY)
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

    /**
     * Data la care a fost Ã®nregistratÄƒ aceastÄƒ stare a efectivului.
     * Fiecare Ã®nregistrare este un snapshot imutabil â€” numÄƒrul de capete
     * nu se actualizeazÄƒ direct, ci se adaugÄƒ un rÃ¢nd nou cu data curentÄƒ.
     * ANSVSA impune datarea explicitÄƒ a fiecÄƒrei modificÄƒri de efectiv.
     */
    @Column(name = "data_inregistrare", nullable = false)
    private LocalDate dataInregistrare = LocalDate.now();

    @Column(name = "detalii")
    private String detalii;

    @Column(name = "tenant_id")
    private String tenantId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EfectivGrup other = (EfectivGrup) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
