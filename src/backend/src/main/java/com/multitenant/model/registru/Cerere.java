package com.multitenant.model.registru;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

import com.multitenant.converter.AesCryptoConverter;
import com.multitenant.util.CryptoUtils;

@Entity
@Table(name = "cereri", indexes = {
    @Index(name = "idx_cereri_cnp_cui_hash", columnList = "cnp_cui_hash")
})
@Getter
@Setter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE cereri SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public class Cerere {
    @jakarta.persistence.Column(nullable = false)
    private boolean deleted = false;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Numele este obligatoriu.")
    @Column(nullable = false, length = 255)
    private String nume;

    @NotBlank(message = "Domiciliul este obligatoriu.")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String domiciliu; // Can store formatted JSON or just text

    @Column(length = 20)
    private String telefon;

    @Column(length = 255)
    private String email;

    @Column(name = "numar_carte_funciara", length = 50)
    private String numarCarteFunciara;

    @Column(name = "numar_cadastral", length = 50)
    private String numarCadastral;

    @Column(name = "cod_cerere", unique = true, nullable = false, length = 50)
    private String codCerere;

    @Column(name = "cnp_cui", length = 255)
    @Convert(converter = AesCryptoConverter.class)
    private String cnpCui;

    @Column(name = "cnp_cui_hash", length = 64)
    private String cnpCuiHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCerere status = StatusCerere.PENDING;

    @Column(name = "user_id")
    private Long userId; // The public.users id if logged in

    @Column(name = "uat_id", nullable = false)
    private Long uatId; // Which UAT this belongs to

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cerere other = (Cerere) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public void setCnpCui(String cnpCui) {
        this.cnpCui = cnpCui;
        this.cnpCuiHash = CryptoUtils.hashSha256(cnpCui);
    }

    @PrePersist
    @PreUpdate
    public void prePersistUpdate() {
        if (this.cnpCui != null) {
            this.cnpCuiHash = CryptoUtils.hashSha256(this.cnpCui);
        } else {
            this.cnpCuiHash = null;
        }
    }
}
