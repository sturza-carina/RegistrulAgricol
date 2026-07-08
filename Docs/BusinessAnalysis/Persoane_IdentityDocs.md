# Business Analysis: Persoane (Persons) & Identity Documents

## 1. Overview
The "Persoane" (Persons) module is the foundational entity of the Agricultural Register (Registru Agricol). It manages the personal data of individuals (physical persons) and entities (legal persons) that are registered as agricultural producers, land owners, or members of a household. This module also securely handles Identity Documents.

## 2. Domain Entities & Attributes

### 2.1. Persoana (Person)
*   **ID**: Unique identifier.
*   **Tip Persoana (Type)**: Fizica (Physical) / Juridica (Legal).
*   **Nume (Last Name)**
*   **Prenume (First Name)**
*   **CNP / CUI**: Personal Identification Number or Company Registration Number.
*   **Data Nasterii (Date of Birth)**
*   **Sex**: Male / Female.
*   **Statut Civil (Civil Status)**
*   **Contact**: Email, Telefon.
*   **Adresa Domiciliu (Domicile Address)**: Street, Number, City, County, Postal Code.

### 2.2. Act de Identitate (Identity Document)
*   **ID Document**: Unique identifier.
*   **Tip Act (Document Type)**: BI (Buletin de Identitate), CI (Carte de Identitate), CIP (Carte de Identitate Provizorie), Pasaport (Passport).
*   **Serie**: Document Series.
*   **Numar**: Document Number.
*   **Emis De (Issued By)**: Issuing Authority (e.g., SPCLEP).
*   **Data Emiterii (Issue Date)**
*   **Data Expirarii (Expiration Date)**
*   **Stare (Status)**: Activ (Active), Expirat (Expired), Anulat (Cancelled).

## 3. Functional Requirements (Use Cases)
*   **UC-P01**: Register a new person (Physical/Legal) in the system.
*   **UC-P02**: Update person's demographic and contact information.
*   **UC-P03**: Add a new Identity Document for a person.
*   **UC-P04**: Flag expiring or expired identity documents.
*   **UC-P05**: Search persons by CNP/CUI or Name.
*   **UC-P06**: View history of identity documents for a person.

## 4. Business Rules & Validations
*   **BR-01 (CNP Validation)**: CNP must be mathematically valid according to the Romanian algorithmic standard and exactly 13 digits long.
*   **BR-02 (CUI Validation)**: CUI must be valid according to the CIF/CUI algorithm.
*   **BR-03 (Uniqueness)**: CNP / CUI must be strictly unique across the entire system.
*   **BR-04 (Age Constraint)**: A person designated as Head of Household (Cap de Gospodarie) must typically be over 18 years old.
*   **BR-05 (Document Expiry)**: The system must not allow the primary active ID to have an expiration date in the past.

## 5. Integrations & Dependencies
*   **Integrates with Household (Gospodarie)**: A person can be a head or member of a household.
*   **Integrates with Citizen Portal (Portal Cetateni)**: Authentication and profile management rely on this data.
*   **Integrates with DEPABD (optional external)**: Potential future API integration for CNP / Address validation.

## 6. GDPR & Security Considerations
*   CNP and ID Document details are PII (Personally Identifiable Information) and must be encrypted at rest.
*   Strict role-based access control (RBAC) required to view ID details.
*   Audit logs must be kept for any read/write operations on Person and ID data.
