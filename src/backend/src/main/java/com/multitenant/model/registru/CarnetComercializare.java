package com.multitenant.model.registru;

import com.multitenant.model.persoana.Persoana;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.envers.Audited;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "carnete_comercializare")
@Getter
@Setter
@NoArgsConstructor
@Audited
@SQLDelete(sql = "UPDATE carnete_comercializare SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public class CarnetComercializare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numar_carnet", unique = true, nullable = false, length = 50)
    private String numarCarnet;

    @Column(nullable = false, length = 10)
    private String seria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atestat_id", nullable = false)
    private AtestatProducator atestat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persoana_id", nullable = false)
    private Persoana persoana;

    @Column(name = "data_eliberare", nullable = false)
    private LocalDate dataEliberare;

    @Column(nullable = false)
    private boolean deleted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
