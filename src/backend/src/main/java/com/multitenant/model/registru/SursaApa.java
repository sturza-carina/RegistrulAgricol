package com.multitenant.model.registru;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "surse_apa")
@Getter
@Setter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE surse_apa SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public class SursaApa {
    @jakarta.persistence.Column(nullable = false)
    private boolean deleted = false;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tip_sursa", length = 100)
    private String tipSursa;

    @Column(name = "debit_mc_ora")
    private Double debitMcOra;

    @Column(name = "stare_functionare", nullable = false)
    private boolean stareFunctionare = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parcela_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private Parcela parcela;

    @Transient
    public Long getParcelaId() {
        return parcela != null ? parcela.getId() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SursaApa other = (SursaApa) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
