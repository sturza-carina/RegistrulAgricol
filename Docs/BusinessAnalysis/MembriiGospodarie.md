# Business Analysis: Membrii Gospodarie (Household Members)

## 1. Overview
The "Membrii Gospodarie" module tracks the composition of the agricultural household. It links individuals (Persoane) to a specific Household (Gospodarie) and defines their relationships and roles within that economic unit.

## 2. Domain Entities & Attributes

### 2.1. Membru Gospodarie (Household Member)
*   **ID Relatie**: Unique identifier for the membership link.
*   **Gospodarie ID**: Reference to the Household.
*   **Persoana ID**: Reference to the Person.
*   **Relatie cu Capul Gospodariei (Relationship to Head)**: Sot/Sotie (Spouse), Fiu/Fiica (Child), Parinte (Parent), Ruda (Relative), Ne-inrudit (Non-relative).
*   **Data Intrarii (Join Date)**: When the person joined the household.
*   **Data Iesirii (Leave Date)**: When the person left (if applicable).
*   **Motiv Iesire (Reason for Leaving)**: Mutare (Moved), Deces (Death), Divort (Divorce).
*   **Mentiuni Speciale (Special Mentions)**: E.g., student, pensioner, working abroad.

## 3. Functional Requirements (Use Cases)
*   **UC-M01**: Add a new member to an existing household.
*   **UC-M02**: Update the relationship status of a member.
*   **UC-M03**: Remove a member from a household (record move/death).
*   **UC-M04**: View the historical composition of a household at a specific past date.
*   **UC-M05**: Automatically transition a member to "Head of Household" upon the death of the previous head.

## 4. Business Rules & Validations
*   **BR-01 (Single Primary Residence)**: A person can only be an active member of ONE primary household at any given time for legal/tax purposes, though they may have secondary agricultural associations.
*   **BR-02 (Head Relationship)**: The person designated as Cap de Gospodarie MUST exist in this table with the relationship "Cap de Gospodarie" (Self).
*   **BR-03 (Orphaned Members)**: A household must not be left without a Head of Household if there are active members remaining.

## 5. Integrations & Dependencies
*   **Persoane**: Sources the demographic data.
*   **Gospodarie**: The target container.
*   **Taxe & Impozite (Taxes)**: Per-capita taxes (e.g., sanitation/garbage collection) rely on the number of active household members.
