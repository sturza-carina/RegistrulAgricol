package com.multitenant.model.persoana;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
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

    @Column(unique = true, length = 13)
    private String cnp;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "persoana", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotAudited
    private List<ActIdentitate> identityDocuments = new ArrayList<>();

    @Column(name = "is_head_of_household")
    private Boolean isHeadOfHousehold;
}


