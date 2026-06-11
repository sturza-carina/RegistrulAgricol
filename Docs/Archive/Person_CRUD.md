# Person CRUD Operations & Data Flow

## Overview

The Person CRUD (Create, Read, Update, Delete) module allows users to manage records in the agricultural registry. Due to the nature of the registry, a "Person" can be either a **Physical Person** or a **Legal Entity**.

This documentation outlines how these operations work across the stack, the data models involved, and how the recent technical issues were resolved.

---

## 1. Data Models (Single Table Inheritance)

The application uses JPA's `SINGLE_TABLE` inheritance strategy to map both `PhysicalPerson` and `LegalEntity` classes to a single `persons` table in the database.

**Base Person Fields**:
- `address`, `phone_number`, `email`
- `register_volume`, `register_position` (Registry-specific tracking)
- `tenant_id` (Ensures data isolation between agricultural registries)
- `person_type` (Discriminator column: `PHYSICAL_PERSON` or `LEGAL_ENTITY`)

**PhysicalPerson Fields**:
- `cnp` (Unique Identifier)
- `first_name`, `last_name`
- `date_of_birth`, `is_head_of_household`

**LegalEntity Fields**:
- `cui` (Unique Identifier)
- `company_name`
- `registration_number`, `legal_representative`

---

## 2. API Endpoints

The backend exposes RESTful endpoints via the `PersonController`.

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/persons` | Retrieves a list of persons. Supports `search` and `type` filters. |
| `GET` | `/api/persons/{id}` | Retrieves a specific person by ID. |
| `POST` | `/api/persons` | Creates a new Person. Returns the created entity. |
| `PUT` | `/api/persons/{id}` | Updates an existing Person. Note: The person type (Physical vs Legal) cannot be changed after creation. |
| `DELETE` | `/api/persons/{id}` | Deletes the specified person. |

*Note: All endpoints implicitly filter data by the current user's Tenant via the `TenantFilter`.*

---

## 3. The Frontend (Angular)

The Angular frontend interfaces with these endpoints through the `PersonService`.
- **List View (`PersonListComponent`)**: Displays all persons in a table. Includes real-time filtering by search query (matching names, emails, CNP/CUI) and Person Type.
- **Form View (`PersonFormComponent`)**: A dynamic reactive form that adapts its fields based on whether the user selects "Physical Person" or "Legal Entity". It handles both Creation and Updating.

---

## 4. Technical Gotchas & Resolutions

During the implementation of the CRUD operations, several technical challenges were resolved:

### A. Hibernate 6 Strict Type Querying (The "Empty Table" Bug)
**Issue:** The `GET /api/persons` request failed silently in the backend, returning 0 results to the frontend.
**Cause:** In `PersonRepository.java`, the HQL query was attempting to compare `p.class = :type` with a string parameter. Hibernate 6 strictly rejects binding a String to a Java Class property.
**Fix:** The discriminator column was explicitly mapped as a read-only field in the `Person` base entity:
```java
@Column(name = "person_type", insertable = false, updatable = false)
private String personType;
```
The repository query was updated to use `p.personType = CAST(:type AS text)`, fixing the search and filtering capabilities.

### B. Database Unique Constraints (The "500 OK" Error)
**Issue:** Submitting a form with a duplicate CNP or CUI resulted in a generic `500 Server Error`.
**Cause:** The database was correctly enforcing the `UNIQUE` constraint, but the backend threw an unhandled `DataIntegrityViolationException`.
**Fix:** A `@ControllerAdvice` (`GlobalExceptionHandler.java`) was implemented to intercept this exception. It translates the SQL constraint violation into a clean `400 Bad Request` with a user-friendly message (e.g., *"A person with this CNP already exists."*). The frontend `PersonFormComponent` was also updated to extract and display this exact message.

### C. Missing Database Columns
**Issue:** The `registerVolume` and `registerPosition` fields were added to the Java entities but missing from the `V2__create_person_tables.sql` migration.
**Fix:** A new Flyway migration script (`V3__add_register_columns_to_persons.sql`) was created and automatically executed across all tenant schemas by the `DatabaseSeeder` on server startup.
