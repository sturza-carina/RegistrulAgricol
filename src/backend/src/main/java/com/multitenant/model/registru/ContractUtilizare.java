package com.multitenant.model.registru;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import com.multitenant.model.core.User;

@Entity
@Table(name = "contract_utilizare")
@Data
@NoArgsConstructor
public class ContractUtilizare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teren_id", nullable = false)
    private Teren teren;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "locator_proprietar_id")
    private User locatorProprietar;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "locator_utilizator_id")
    private User locatorUtilizator;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "utilizator_operare_id")
    private User utilizatorOperare;

    @Column(name = "este_activ")
    private boolean esteActiv = true;
}
