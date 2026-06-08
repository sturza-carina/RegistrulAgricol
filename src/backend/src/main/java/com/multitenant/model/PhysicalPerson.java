package com.multitenant.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("PHYSICAL_PERSON")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PhysicalPerson extends Person {

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(unique = true, length = 13)
    private String cnp;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IdentityDocument> identityDocuments = new ArrayList<>();

    @Column(name = "is_head_of_household")
    private Boolean isHeadOfHousehold;
}
