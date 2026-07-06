package com.multitenant.model.registru;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "documente")
@Getter
@Setter
@NoArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gospodarie_id", nullable = false)
    private Gospodarie gospodarie;

    @Column(name = "tip_document_id", nullable = false)
    private Integer tipDocumentId;

    @Column(name = "nume_fisier", nullable = false, length = 255)
    private String numeFisier;

    @Column(name = "cale_stocare", nullable = false, length = 500)
    private String caleStocare;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "dimensiune_kb")
    private Integer dimensiuneKb;

    @Column(name = "data_emitere")
    private LocalDate dataEmitere;

    @Column(name = "data_expirare")
    private LocalDate dataExpirare;

    @Column(name = "uploaded_by_id")
    private Long uploadedById;

    @Column(name = "data_incarcare")
    private LocalDateTime dataIncarcare = LocalDateTime.now();

    @Column(name = "observatii", columnDefinition = "TEXT")
    private String observatii;

    @Column(name = "este_activ")
    private boolean esteActiv = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document other = (Document) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}