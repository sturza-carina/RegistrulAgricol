package com.multitenant.model.registru;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "cereri")
@Getter
@Setter
@NoArgsConstructor
public class Cerere {

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

    @Column(name = "cnp_cui", length = 50)
    private String cnpCui;

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
}
