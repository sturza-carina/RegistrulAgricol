package com.multitenant.model.registru;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import com.multitenant.model.common.Adresa;
import com.multitenant.model.core.Uat;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import com.multitenant.model.persoana.Persoana;

@Entity
@Table(name = "gospodarii")
@Getter
@Setter
@NoArgsConstructor
@Audited
@SQLDelete(sql = "UPDATE gospodarii SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public class Gospodarie {
    @jakarta.persistence.Column(nullable = false)
    private boolean deleted = false;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cap_gospodarie_id")
    private Persoana capGospodarie;

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
