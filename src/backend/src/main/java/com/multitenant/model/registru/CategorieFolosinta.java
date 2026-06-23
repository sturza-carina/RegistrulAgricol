package com.multitenant.model.registru;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categorie_folosinta")
@Data
@NoArgsConstructor
public class CategorieFolosinta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String denumire;

    @Column(columnDefinition = "TEXT")
    private String descriere;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teren_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Teren teren;

    @Transient
    public Long getTerenId() {
        return teren != null ? teren.getId() : null;
    }
}
