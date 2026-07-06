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
@Table(name = "atestate_producator")
@Getter
@Setter
@NoArgsConstructor
@Audited
@SQLDelete(sql = "UPDATE atestate_producator SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public class AtestatProducator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numar_atestat", unique = true, nullable = false, length = 50)
    private String numarAtestat;

    @Column(nullable = false, length = 10)
    private String seria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persoana_id", nullable = false)
    private Persoana persoana;

    @Column(name = "data_eliberare", nullable = false)
    private LocalDate dataEliberare;

    @Column(name = "valabilitate_luni", nullable = false)
    private Integer valabilitateLuni = 60; // Usually valid for 5 years

    @Column(columnDefinition = "TEXT")
    private String observatii;

    @Column(nullable = false)
    private boolean deleted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
