package com.multitenant.model.core;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "cetateni", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class Cetatean {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nume;

    @Column(nullable = false, length = 255)
    private String prenume;

    @Column(nullable = false, length = 13, unique = true)
    private String cnp;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String parola;

    @Column(nullable = false, length = 50)
    private String telefon;

    @Column(nullable = false, length = 255)
    private String judet;

    @Column(nullable = false, length = 255)
    private String localitate;

    @Column(nullable = false, length = 255)
    private String strada;

    @Column(nullable = false, length = 50)
    private String numar;

    @Column(length = 50)
    private String bloc;

    @Column(length = 50)
    private String scara;

    @Column(length = 50)
    private String etaj;

    @Column(length = 50)
    private String apartament;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
