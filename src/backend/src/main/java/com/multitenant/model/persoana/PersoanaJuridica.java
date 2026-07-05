package com.multitenant.model.persoana;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import com.multitenant.converter.AesCryptoConverter;
import com.multitenant.util.CryptoUtils;

@Entity
@DiscriminatorValue("LEGAL_ENTITY")
@Getter
@Setter
@NoArgsConstructor
@Audited
public class PersoanaJuridica extends Persoana {

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "cui", length = 255)
    @Convert(converter = AesCryptoConverter.class)
    private String cui;

    @Column(name = "cui_hash", length = 64)
    private String cuiHash;

    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(name = "legal_representative")
    private String legalRepresentative;

    public void setCui(String cui) {
        this.cui = cui;
        this.cuiHash = CryptoUtils.hashSha256(cui);
    }

    @PrePersist
    @PreUpdate
    public void prePersistUpdate() {
        if (this.cui != null) {
            this.cuiHash = CryptoUtils.hashSha256(this.cui);
        } else {
            this.cuiHash = null;
        }
    }
}

