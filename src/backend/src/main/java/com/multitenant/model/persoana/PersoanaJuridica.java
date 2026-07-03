package com.multitenant.model.persoana;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

@Entity
@DiscriminatorValue("LEGAL_ENTITY")
@Getter
@Setter
@NoArgsConstructor
@Audited
public class PersoanaJuridica extends Persoana {

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "cui", unique = true)
    private String cui;

    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(name = "legal_representative")
    private String legalRepresentative;
}

