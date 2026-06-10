package com.multitenant.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "person_relations")
@Data
@NoArgsConstructor
public class RelatieRudenie {

    public enum KinshipType {
        PARENT,
        CHILD,
        SPOUSE,
        SIBLING,
        GRANDPARENT,
        GRANDCHILD,
        OTHER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    @JsonIgnore
    private Persoana persoana;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "related_person_id")
    @JsonIgnoreProperties({"relations", "identityDocuments"})
    private Persoana relatedPerson;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type")
    private KinshipType relationType;
}


