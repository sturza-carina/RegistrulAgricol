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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "persoana", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotAudited
    private List<ActIdentitate> identityDocuments = new ArrayList<>();

    @Column(name = "is_head_of_household")
    private Boolean isHeadOfHousehold;

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


