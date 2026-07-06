package com.multitenant.model.core;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Global UAT master registry â€” stored in public.uat.
 * Managed exclusively by ROLE_SUPER_ADMIN.
 * Tenant admins "claim" UATs from this list into their local tenant schema's uat table.
 */
@Entity
@Table(name = "uats", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@SQLDelete(sql = "UPDATE uats SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public class PublicUat {
    @jakarta.persistence.Column(nullable = false)
    private boolean deleted = false;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cod_siruta", nullable = false, length = 50)
    private String codSiruta;

    @Column(nullable = false, length = 255)
    private String denumire;

    @Column(nullable = false, length = 100)
    private String judet;

    @Column(name = "tip_uat", nullable = false, length = 50)
    private String tipUat;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Soft-reference to which tenant this UAT is primarily associated with.
    // The actual authoritative assignment lives in tenant_X.uat.
    @Column(name = "tenant_id")
    private String tenantId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicUat other = (PublicUat) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
