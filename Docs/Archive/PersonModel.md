# Person Domain Model

The `Person` domain model in the Registrul Agricol application represents any legal subject that can hold or own agricultural properties. It is implemented using an object-oriented inheritance hierarchy mapped to a single database table (`persons`) via JPA's `SINGLE_TABLE` inheritance strategy.

## 1. Abstract Base Class: `Person`

The `Person` class is an abstract `@Entity` that contains all the common attributes shared across both individuals and companies.

### Common Attributes
- **Address Details (Domiciliu/Sediu Social):** Embedded as `Address` class containing `county`, `locality`, `street`, `streetNumber`, `building`, `staircase`, `floor` (Integer), `apartmentNumber` (Integer), `postalCode`.
- **Contact Information:** `phoneNumber`, `email`.
- **Agricultural Register Details:**
  - `notes`: Any additional observation notes.
- **Relations:**
  - `relations`: A `@OneToMany` relationship to `PersonRelation` entities, which explicitly define how this person is related to other `Person`s in the database using the `PersonRelation.KinshipType` enum.
- **Multi-Tenancy:** `tenantId` is used to segregate records per local administration.

---

## 2. Concrete Class: `PhysicalPerson`

Represents an individual ("Persoană Fizică"). 
- **Discriminator Value:** `PHYSICAL_PERSON`

### Specific Attributes
- `firstName` & `lastName`
- `cnp` (Cod Numeric Personal - 13 characters, unique)
- `dateOfBirth`
- **Household Role:**
  - `isHeadOfHousehold`: Boolean flag indicating if they are the primary holder of the household.
- **Identity Documents:** A one-to-many relationship (`List<IdentityDocument>`) linking the person to their various identity documents.

---

## 3. Concrete Class: `LegalEntity`

Represents a company, institution, or association ("Persoană Juridică").
- **Discriminator Value:** `LEGAL_ENTITY`

### Specific Attributes
- `companyName` (Denumire)
- `cui` (Cod Unic de Înregistrare - unique)
- `registrationNumber` (Număr de înregistrare la Registrul Comerțului, e.g., J40/...)
- `legalRepresentative` (Name of the authorized representative)

---

## 4. Identity Documents

To support the diverse types of identification a `PhysicalPerson` might hold, the application uses a single `IdentityDocument` entity with a `documentType` field to distinguish between them (e.g., Identity Card, Birth Certificate, Passport, Driver License).

A `PhysicalPerson` is linked via a `@OneToMany` relationship to the `IdentityDocument` class.

### `IdentityDocument` Fields
Contains the following fields for any official document:
- `documentType` (using the `IdentityDocument.IdentityCardType` enum)
- `series`
- `number`
- `issuedBy`
- `issueDate`
- `expirationDate`
