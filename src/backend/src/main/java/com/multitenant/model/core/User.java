package com.multitenant.model.core;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users", schema = "public")
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // SUPER_ADMIN, ADMIN, USER

    @Column(name = "tenant_id")
    private String tenantId;

    @Column(length = 255)
    private String nume;

    @Column(length = 255)
    private String email;

    @Column(nullable = false)
    private boolean activ = true;

    // Plain Long — references uat.id in the tenant schema (no JPA cross-schema FK)
    @Column(name = "uat_id")
    private Long uatId;
}
