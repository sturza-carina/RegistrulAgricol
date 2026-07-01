package com.multitenant.model.core;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "users", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User other = (User) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

