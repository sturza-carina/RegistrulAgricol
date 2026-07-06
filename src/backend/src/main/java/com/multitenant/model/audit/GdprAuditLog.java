package com.multitenant.model.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "gdpr_audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class GdprAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    private String utilizator;

    @Enumerated(EnumType.STRING)
    @Column(name = "tip_actiune", nullable = false, length = 50)
    private GdprActionType tipActiune;

    @Column(name = "entitate_vizata", nullable = false)
    private String entitateVizata;

    @Column(name = "id_persoana_vizata", length = 1024)
    private String idPersoanaVizata;

    @Column(length = 512)
    private String endpoint;

    @Column(name = "tenant_id")
    private String tenantId;
}
