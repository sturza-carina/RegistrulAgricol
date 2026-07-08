# Business Analysis: Portal Cetateni (Citizen Portal)

## 1. Overview
The "Portal Cetateni" is the public-facing interface (frontend) that allows citizens (physical or legal persons) to interact with the Agricultural Register digitally. It provides self-service capabilities, reducing foot traffic to the mayor's office (Primarie).

## 2. Key Features & Capabilities

### 2.1. Authentication & Authorization
*   **e-ID / ROeID Integration**: Login using national electronic identity systems.
*   **Account Linking**: Linking a digital portal account to an existing "Persoana" entity via CNP validation.
*   **MFA (Multi-Factor Authentication)**: SMS or Email OTP for secure access.

### 2.2. Self-Service Declarations
*   Citizens can submit their annual agricultural declarations (Declaratii anuale).
*   Update crop types, animal counts, and machinery owned.

### 2.3. Certificate Requests
*   Request "Adeverinta de Rol Agricol" (Agricultural Role Certificate).
*   Download digitally signed (PKI) certificates instantly if data is up-to-date.

### 2.4. Notifications & Alerts
*   Receive alerts for expiring lease contracts, missing annual declarations, or document requests from clerks.

## 3. Functional Requirements (Use Cases)
*   **UC-PC01**: Citizen logs into the portal and views their current household data.
*   **UC-PC02**: Citizen initiates a new "Cerere" (Request) for a certificate.
*   **UC-PC03**: Citizen submits a form to update land use categories for the new agricultural year.
*   **UC-PC04**: Citizen tracks the status of their submitted requests.

## 4. Business Rules & Validations
*   **BR-01 (Data Freeze)**: Citizens cannot retroactively modify declarations from previous agricultural years.
*   **BR-02 (Clerk Approval)**: Changes to critical data (e.g., selling land, removing a household member) submitted via the portal remain in a "Pending" state until a clerk validates the attached proof (e.g., sale contract, death certificate).
*   **BR-03 (Access Control)**: A citizen can only view data for the Gospodarie where they are listed as Head or authorized Member.

## 5. Integrations & Dependencies
*   **Cereri (Requests Module)**: All portal actions generate tickets/requests in the back-office.
*   **Persoane & Gospodarie**: Core data sources.
*   **Digital Signature Provider**: For issuing legally binding certificates (e.g., Trans Sped, CertSign).
