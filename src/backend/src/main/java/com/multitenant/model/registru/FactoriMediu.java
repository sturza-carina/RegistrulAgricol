package com.multitenant.model.registru;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "factori_mediu")
@Getter
@Setter
@NoArgsConstructor
public class FactoriMediu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parcela_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private Parcela parcela;

    @Column(name = "temperatura")
    private Double temperatura;

    @Column(name = "umiditate_relativa")
    private Double umiditateRelativa;

    @Column(name = "data_inregistrare", nullable = false)
    private LocalDateTime dataInregistrare;

    @Transient
    public Long getParcelaId() {
        return parcela != null ? parcela.getId() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FactoriMediu other = (FactoriMediu) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
