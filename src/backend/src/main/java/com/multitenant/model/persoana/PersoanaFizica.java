package com.multitenant.model.persoana;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import com.multitenant.converter.AesCryptoConverter;
import com.multitenant.util.CryptoUtils;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("PHYSICAL_PERSON")
@Getter
@Setter
@NoArgsConstructor
@Audited
public class PersoanaFizica extends Persoana {

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "cnp", length = 255)
    @Convert(converter = AesCryptoConverter.class)
    private String cnp;

    @Column(name = "cnp_hash", length = 64)
    private String cnpHash;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "este_decedat")
    private Boolean esteDecedat = false;

    @Column(name = "data_decesului")
    private LocalDate dataDecesului;

    @Column(name = "numar_certificat_deces")
    private String numarCertificatDeces;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "persoana", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotAudited
    private List<ActIdentitate> identityDocuments = new ArrayList<>();

    public static String generateBlindIndex(String cnp) {
        return CryptoUtils.hashSha256(cnp);
    }

    public void setCnp(String cnp) {
        this.cnp = cnp;
        this.cnpHash = CryptoUtils.hashSha256(cnp);
    }

    @PrePersist
    @PreUpdate
    public void prePersistUpdate() {
        if (this.cnp != null) {
            this.cnpHash = CryptoUtils.hashSha256(this.cnp);
        } else {
            this.cnpHash = null;
        }
    }
}


