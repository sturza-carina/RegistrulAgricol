package com.multitenant.model;

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

    // Optional relation for tracking. The physical FK is in the DB.
    @Column(name = "tenant_id")
    private String tenantId;

    @Column(length = 255)
    private String nume;

    @Column(length = 255)
    private String email;

    @Column(nullable = false)
    private boolean activ = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uat_id")
    private Uat uat;
}
