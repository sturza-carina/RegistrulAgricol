package com.multitenant.service;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.model.*;
import com.multitenant.repository.PersonRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class PersonService {

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public Person createPerson(Person person) {
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals("public")) {
            person.setTenantId(currentTenant);
        }

        // Set parent references and validate relations
        if (person instanceof PhysicalPerson physicalPerson) {
            if (physicalPerson.getIdentityDocuments() != null) {
                for (IdentityDocument doc : physicalPerson.getIdentityDocuments()) {
                    doc.setPerson(physicalPerson);
                    doc.setTenantId(currentTenant);
                }
            }
        }

        if (person.getRelations() != null) {
            for (PersonRelation relation : person.getRelations()) {
                relation.setPerson(person);
                validateAndSetRelatedPerson(relation);
            }
        }

        return personRepository.save(person);
    }

    @Transactional(readOnly = true)
    public List<Person> getAllPersons(String search, String type) {
        return personRepository.searchPersons(search, type);
    }

    @Transactional(readOnly = true)
    public Person getPersonById(Long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Person not found with id: " + id));
    }

    public Person updatePerson(Long id, Person updatedPerson) {
        Person existing = getPersonById(id);

        if (!existing.getClass().equals(updatedPerson.getClass())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot change person type");
        }

        String currentTenant = TenantContext.getCurrentTenant();

        // Update common fields
        existing.setAddress(updatedPerson.getAddress());
        existing.setPhoneNumber(updatedPerson.getPhoneNumber());
        existing.setEmail(updatedPerson.getEmail());
        existing.setRegisterVolume(updatedPerson.getRegisterVolume());
        existing.setRegisterPosition(updatedPerson.getRegisterPosition());
        existing.setNotes(updatedPerson.getNotes());

        // Update type-specific fields
        if (existing instanceof PhysicalPerson existingPhysical && updatedPerson instanceof PhysicalPerson updatedPhysical) {
            existingPhysical.setFirstName(updatedPhysical.getFirstName());
            existingPhysical.setLastName(updatedPhysical.getLastName());
            existingPhysical.setCnp(updatedPhysical.getCnp());
            existingPhysical.setDateOfBirth(updatedPhysical.getDateOfBirth());
            existingPhysical.setIsHeadOfHousehold(updatedPhysical.getIsHeadOfHousehold());

            // Merge identity documents using orphanRemoval
            existingPhysical.getIdentityDocuments().clear();
            if (updatedPhysical.getIdentityDocuments() != null) {
                for (IdentityDocument doc : updatedPhysical.getIdentityDocuments()) {
                    doc.setPerson(existingPhysical);
                    doc.setTenantId(currentTenant);
                    existingPhysical.getIdentityDocuments().add(doc);
                }
            }
        } else if (existing instanceof LegalEntity existingLegal && updatedPerson instanceof LegalEntity updatedLegal) {
            existingLegal.setCompanyName(updatedLegal.getCompanyName());
            existingLegal.setCui(updatedLegal.getCui());
            existingLegal.setRegistrationNumber(updatedLegal.getRegistrationNumber());
            existingLegal.setLegalRepresentative(updatedLegal.getLegalRepresentative());
        }

        // Merge relations using orphanRemoval
        existing.getRelations().clear();
        if (updatedPerson.getRelations() != null) {
            for (PersonRelation relation : updatedPerson.getRelations()) {
                relation.setPerson(existing);
                validateAndSetRelatedPerson(relation);
                existing.getRelations().add(relation);
            }
        }

        return personRepository.save(existing);
    }

    public void deletePerson(Long id) {
        Person person = getPersonById(id);
        personRepository.delete(person);
    }

    private void validateAndSetRelatedPerson(PersonRelation relation) {
        if (relation.getRelatedPerson() == null || relation.getRelatedPerson().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Related person must have a valid ID");
        }
        Long relatedId = relation.getRelatedPerson().getId();
        Person related = personRepository.findById(relatedId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Related person not found with id: " + relatedId));
        relation.setRelatedPerson(related);
    }
}
