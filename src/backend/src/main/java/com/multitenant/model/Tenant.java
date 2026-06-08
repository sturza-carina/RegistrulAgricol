package com.multitenant.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tenants", schema = "public")
@Data
@NoArgsConstructor
public class Tenant {
    @Id
    @Column(name = "id")
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(name = "schema_name", nullable = false, unique = true)
    private String schemaName;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
