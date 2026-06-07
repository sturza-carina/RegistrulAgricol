# Person Domain Model

The `Person` domain model in the Registrul Agricol application represents any legal subject that can hold or own agricultural properties. It is implemented using an object-oriented inheritance hierarchy mapped to a single database table (`persons`) via JPA's `SINGLE_TABLE` inheritance strategy.

## 1. Abstract Base Class: `Person`

The `Person` class is an abstract `@Entity` that contains all the common attributes shared across both individuals and companies.

### Common Attributes
- **Address Details (Domiciliu/Sediu Social):** `county`, `city`, `village`, `street`, `streetNumber`, `block`, `staircase`, `floor`, `apartment`, `postalCode`.
- **Contact Information:** `phoneNumber`, `email`.
- **Agricultural Register Details:**
  - `registerVolume`: The physical or digital volume number in the Agricultural Register.
  - `registerPosition`: The specific position/page in the volume.
  - `notes`: Any additional observation notes.
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
  - `kinshipRelation`: If they are not the head, this describes their relationship to the head of the household.
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

## 4. Identity Documents Hierarchy

To support the diverse types of identification a `PhysicalPerson` might hold, the application uses a secondary inheritance hierarchy for identity documents. 

A `PhysicalPerson` is linked via a `@OneToMany` relationship to the `IdentityDocument` abstract base class.

### Abstract Base: `IdentityDocument`
Contains common fields for any official document:
- `series`
- `number`
- `issuedBy`
- `issueDate`

### Concrete Document Types
The following concrete classes extend `IdentityDocument` and are mapped using the `SINGLE_TABLE` strategy in the `identity_documents` table:
- `IdentityCard` (Carte/Buletin de Identitate)
- `BirthCertificate` (Certificat de Naștere)
- `Passport` (Pașaport)
- `DriverLicense` (Permis de Conducere)
