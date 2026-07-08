# Business Analysis: Carte Funciara (Land Registry / Title Deeds)

## 1. Overview
The "Carte Funciara" (CF) module handles the legal ownership documents and land registry entries. It acts as the bridge between the administrative Agricultural Register and the legal cadastre (ANCPI - National Agency for Cadastre and Land Registration).

## 2. Domain Entities & Attributes

### 2.1. Inscriere Carte Funciara (Land Registry Entry)
*   **ID CF**: Unique systemic identifier.
*   **Numar Carte Funciara (CF Number)**: The legal book number.
*   **Numar Cadastral (Cadastral Number)**: The legal property identifier.
*   **UAT (Admin Unit)**
*   **Proprietari (Owners)**: Array/List of Person IDs with ownership quotas (e.g., 1/2, 1/1).
*   **Suprafata Din Acte (Area on Title)**: Legal area size.
*   **Suprafata Masurata (Measured Area)**: Actual measured area.
*   **Sarcini / Ipoteci (Encumbrances / Mortgages)**: True/False flag or details.
*   **Stare Document (Document Status)**: Intabulat (Registered), In curs (Pending).

## 3. Functional Requirements (Use Cases)
*   **UC-CF01**: Record a new Carte Funciara associated with a specific Parcela or Teren.
*   **UC-CF02**: Update ownership quotas upon inheritance (Dezbatere mostenire) or sale.
*   **UC-CF03**: Upload and store digital copies of the CF Extract (Extras de Carte Funciara).
*   **UC-CF04**: Reconcile differences between "Area on Title" and physical "Parcel Area".

## 4. Business Rules & Validations
*   **BR-01 (Quota Validation)**: The sum of ownership quotas for a single Cadastral Number must equal 100% (or 1/1).
*   **BR-02 (Area Discrepancy)**: If the declared parcel area exceeds the CF area, the system must trigger a warning for manual clerk review.
*   **BR-03 (Mandatory Cadastre)**: New entries for land transactions post-2010 usually mandate a valid Cadastral Number.

## 5. Integrations & Dependencies
*   **Parcele**: Each CF entry maps to one or more Parcele.
*   **ANCPI**: Potential external API integration to fetch real-time CF Extracts.
*   **Document Management**: Integration with file storage for scanned deeds.
