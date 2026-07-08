# Business Analysis: Gospodarie (Agricultural Household)

## 1. Overview
The "Gospodarie" (Household) is the core operational unit in the Agricultural Register. It represents the socio-economic unit formed by family members or individuals living together, contributing to the agricultural exploitation of land and raising animals.

## 2. Domain Entities & Attributes

### 2.1. Gospodarie (Household)
*   **ID Gospodarie**: Unique systemic identifier.
*   **Numar Registru (Register Number)**: Unique logical number (Volum, Pozitie) according to physical register standards.
*   **Rol Nominal Unic (Unique Nominal Role)**: Financial / Tax identifier.
*   **Tip Gospodarie**: Individuala (Individual), Asociatie (Association), etc.
*   **Adresa Gospodarie (Location)**: Complete address of the physical household.
*   **Cap Gospodarie (Head of Household)**: Reference to a `Persoana` entity.
*   **Stare (Status)**: Activa (Active), Radiata (Archived/Closed), Inactiva (Inactive).
*   **Data Deschiderii (Opening Date)**: Date when the household was registered.
*   **Data Incheierii (Closing Date)**: Date when the household was closed (if applicable).

## 3. Functional Requirements (Use Cases)
*   **UC-G01**: Open a new Agricultural Household (Alocare Numar / Pozitie).
*   **UC-G02**: Assign / Change the Head of the Household.
*   **UC-G03**: Transfer the household (e.g., in case of inheritance or sale).
*   **UC-G04**: Radiate (close) a household due to relocation, death of sole member, or merger.
*   **UC-G05**: Generate household reports ("Adeverinta de rol agricol").

## 4. Business Rules & Validations
*   **BR-01 (Head Requirement)**: Every active household MUST have exactly one designated Head of Household.
*   **BR-02 (Unique Register Position)**: The combination of Volum and Pozitie must be unique per administrative-territorial unit (UAT).
*   **BR-03 (Closure Constraints)**: A household cannot be closed (radiata) if it has active land (terenuri) or animals attached to it. Assets must be transferred or zeroed out first.

## 5. Integrations & Dependencies
*   **Membrii Gospodarie**: Directly links to the members living in the household.
*   **Terenuri & Parcele**: Owns or exploits agricultural and non-agricultural lands.
*   **Animale**: Owns livestock.
*   **Taxe & Impozite**: Integrating the 'Rol Nominal Unic' for tax calculations.
