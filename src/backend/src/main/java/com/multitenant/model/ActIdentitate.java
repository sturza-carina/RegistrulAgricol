package com.multitenant.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "identity_documents")
@Data
@NoArgsConstructor
public class ActIdentitate {

    public enum IdentityCardType {
        IDENTITY_CARD,
        BIRTH_CERTIFICATE,
        PASSPORT,
        DRIVER_LICENSE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    private IdentityCardType documentType;

    private String series;

    @Column(name = "document_number")
    private String number;

    @Column(name = "issued_by")
    private String issuedBy;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    // Optional relation for tracking multi-tenancy.
    @Column(name = "tenant_id")
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    @JsonIgnore
    private PersoanaFizica persoana;
}

