package com.multitenant.model.registru;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import com.multitenant.model.persoana.Persoana;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "istoric_membri_gospodarie")
@Getter
@Setter
@NoArgsConstructor
@Audited
@SQLDelete(sql = "UPDATE istoric_membri_gospodarie SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public class IstoricMembruGospodarie {

    @jakarta.persistence.Column(nullable = false)
    private boolean deleted = false;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gospodarie_id", nullable = false)
    private Gospodarie gospodarie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persoana_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Persoana persoana;

    @Enumerated(EnumType.STRING)
    @Column(name = "tip_eveniment", nullable = false, length = 50)
    private TipEvenimentMembru tipEveniment;

    @Column(name = "data_eveniment", nullable = false)
    private LocalDate dataEveniment;

    @Column(name = "observatii", columnDefinition = "TEXT")
    private String observatii;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
