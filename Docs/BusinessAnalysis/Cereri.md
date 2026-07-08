# Business Analysis: Cereri (Requests / Petitions / Ticketing)

## 1. Overview
The "Cereri" module acts as the workflow and ticketing engine for the Agricultural Register. It manages all requests initiated by citizens (via the Portal or at the physical counter) and tracks them through their lifecycle until resolution by the municipal clerks.

## 2. Domain Entities & Attributes

### 2.1. Cerere (Request)
*   **Numar Inregistrare (Registration Number)**: Unique registry number (Numar de inregistrare Primarie).
*   **Data Depunerii (Submission Date)**
*   **Tip Cerere (Request Type)**: Eliberare Adeverinta (Certificate Issuance), Modificare Date (Data Amendment), Deschidere Rol (Open Role), Radiere (Closure).
*   **Solicitant (Applicant)**: Link to Persoana ID.
*   **Gospodarie ID**: Relevant household.
*   **Stare (Status)**: Noua (New), In Lucru (In Progress), Aprobata (Approved), Respinsa (Rejected), Finalizata (Completed).
*   **Functionar Asignat (Assigned Clerk)**: System User ID.
*   **Termen Legal (Legal Deadline)**: Date by which it must be resolved (e.g., 30 days).

### 2.2. Documente Atasate (Attachments)
*   User-uploaded proofs (ID scans, contracts, deeds).
*   System-generated outputs (Draft certificates).

## 3. Functional Requirements (Use Cases)
*   **UC-C01**: Register a new request (automatically from Portal or manually by clerk).
*   **UC-C02**: Route request to the appropriate department/clerk based on "Tip Cerere".
*   **UC-C03**: Approve a data amendment request, automatically applying the changes to the core databases.
*   **UC-C04**: Reject a request and send a justification message to the citizen.
*   **UC-C05**: Generate an audit trail of all state changes for a request.

## 4. Business Rules & Validations
*   **BR-01 (Legal Deadlines)**: The system must visually flag requests that are within 5 days of their Legal Deadline.
*   **BR-02 (Mandatory Attachments)**: Certain request types (e.g., "Deschidere Rol") cannot be submitted without specific mandatory document types (e.g., Title Deed, ID).
*   **BR-03 (State Transitions)**: A request cannot move from "Aprobata" back to "Noua". State transitions are strictly one-way forward, with specific rollback protocols requiring supervisor overrides.

## 5. Integrations & Dependencies
*   **Portal Cetateni**: Primary source of incoming requests.
*   **Registratura Generala (General Registry)**: Integration with the Town Hall's main document registry system to sync "Numar Inregistrare".
*   **All Core Modules**: Approved requests trigger data updates in Persoane, Terenuri, Parcele, etc.
