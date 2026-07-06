package com.multitenant.model.registru;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import com.multitenant.model.persoana.Persoana;

@Entity
@Table(name = "contracte_utilizare")
@Getter
@Setter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE contracte_utilizare SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public class ContractUtilizare {
    @jakarta.persistence.Column(nullable = false)
    private boolean deleted = false;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcela_id", nullable = false)
    private Parcela parcela;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locator_proprietar_id")
    private Persoana locatorProprietar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locator_utilizator_id")
    private Persoana locatorUtilizator;

    @Enumerated(EnumType.STRING)
    @Column(name = "tip_contract", nullable = false, length = 50)
    private TipContractUtilizare tipContract;

    @Column(name = "numar_contract", nullable = false, length = 100)
    private String numarContract;

    @Column(name = "data_semnare")
    private LocalDate dataSemnare;

    @Column(name = "data_inceput")
    private LocalDate dataInceput;

    @Column(name = "data_sfarsit")
    private LocalDate dataSfarsit;

    @Column(name = "pret_arenda_ron_an")
    private Double pretArendaRonAn;

    @Column(name = "pret_arenda_grau_kg_ha")
    private Double pretArendaGrauKgHa;

    @Column(name = "indexare_pret")
    private boolean indexarePret = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_contract", length = 50)
    private StatusContractUtilizare statusContract = StatusContractUtilizare.ACTIV;

    @Column(name = "motiv_incetare", columnDefinition = "TEXT")
    private String motivIncetare;

    @Column(name = "data_operare")
    private LocalDate dataOperare = LocalDate.now();

    @Column(name = "utilizator_operare_id")
    private Long utilizatorOperareId;

    @Column(name = "este_activ")
    private boolean esteActiv = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContractUtilizare other = (ContractUtilizare) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
