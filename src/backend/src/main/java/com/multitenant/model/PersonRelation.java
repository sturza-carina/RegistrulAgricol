package com.multitenant.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "person_relations")
@Data
@NoArgsConstructor
public class PersonRelation {

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
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_person_id")
    private Person relatedPerson;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type")
    private KinshipType relationType;
}
