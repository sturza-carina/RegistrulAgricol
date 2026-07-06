package com.multitenant.model.registru;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import com.multitenant.model.common.Adresa;
import com.multitenant.model.core.Uat;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Table(name = "gospodarii")
@Getter
@Setter
@NoArgsConstructor
@Audited
public class Gospodarie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cod_gospodarie", nullable = false, length = 100)
    private String codGospodarie;

    @Embedded
    private Adresa adresa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tip_gospodarie", nullable = false, length = 50)
    private TipGospodarie tipGospodarie;

    @Column(nullable = false)
    private boolean activa = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uat_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Uat uat;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gospodarie other = (Gospodarie) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
