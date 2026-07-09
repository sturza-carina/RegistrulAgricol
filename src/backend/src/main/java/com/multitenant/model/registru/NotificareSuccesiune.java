package com.multitenant.model.registru;

import com.multitenant.model.persoana.PersoanaFizica;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificari_succesiuni")
@Getter
@Setter
@NoArgsConstructor
public class NotificareSuccesiune {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "defunct_id", nullable = false)
    private PersoanaFizica defunct;

    @Column(name = "defunct_cnp_hash", nullable = false, length = 64)
    private String defunctCnpHash;

    @Column(name = "nume_notar_spn_bin")
    private String numeNotarSpnBin;

    @Column(name = "numar_adresa_oficiala", length = 100)
    private String numarAdresaOficiala;

    @Column(name = "data_trimitere")
    private LocalDate dataTrimitere;

    @Column(name = "stadiu_notificare", nullable = false, length = 50)
    private String stadiuNotificare; // TRIMIS, IN_LUCRU, FINALIZAT

    @Column(columnDefinition = "TEXT")
    private String observatii;

    @Column(name = "utilizator_operare")
    private String utilizatorOperare;

    @Column(name = "data_inregistrare")
    private LocalDateTime dataInregistrare;

    @PrePersist
    @PreUpdate
    public void prePersistUpdate() {
        if (this.defunct != null) {
            this.defunctCnpHash = this.defunct.getCnpHash();
        }
        if (this.dataInregistrare == null) {
            this.dataInregistrare = LocalDateTime.now();
        }
    }
}
