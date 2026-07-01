package com.multitenant.model.registru;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categorii_folosinta")
@Getter
@Setter
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategorieFolosinta other = (CategorieFolosinta) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
