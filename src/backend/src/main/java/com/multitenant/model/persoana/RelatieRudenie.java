package com.multitenant.model.persoana;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "person_relations")
@Getter
@Setter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE person_relations SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public class RelatieRudenie {
    @jakarta.persistence.Column(nullable = false)
    private boolean deleted = false;


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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_person_id")
    @JsonIgnoreProperties({"relations", "identityDocuments"})
    private Persoana relatedPerson;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type")
    private KinshipType relationType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelatieRudenie other = (RelatieRudenie) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}


