package com.multitenant.model.registru;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "surse_apa")
@Data
@NoArgsConstructor
public class SursaApa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tip_sursa", length = 100, nullable = false)
    private TipSursaApa tipSursa;

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
}
