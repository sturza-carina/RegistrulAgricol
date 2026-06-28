package com.multitenant.model.core;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Global UAT master registry — stored in public.uat.
 * Managed exclusively by ROLE_SUPER_ADMIN.
 * Tenant admins "claim" UATs from this list into their local tenant schema's uat table.
 */
@Entity
@Table(name = "uats", schema = "public")
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PublicUat {
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
}
