# Business Analysis: Terenuri (Lands & Land Categories)

## 1. Overview
The "Terenuri" (Lands) module manages the overall land owned or exploited by a household (Gospodarie) or legal entity. This tracks land on a macro level, focusing on the categories of use (Categorie Folosinta), total surfaces, and ownership types (In proprietate, In arenda, etc.).

## 2. Domain Entities & Attributes

### 2.1. Teren (Land Holding)
*   **ID Teren**: Unique identifier.
*   **Gospodarie ID**: Link to the Household owning/using the land.
*   **Tip Proprietate (Ownership Type)**: Proprietate (Owned), Arenda (Leased), Concesiune (Concession), etc.
*   **Suprafata Totala (Total Area)**: Expressed in Hectares (ha) and Ares (ar) or Square Meters (mp).
*   **Localizare UAT**: Which Administrative Territorial Unit the land is in.

### 2.2. Categorie Folosinta (Category of Use)
*   **Arabil (Arable)**
*   **Pasuni (Pastures)**
*   **Fanete (Meadows)**
*   **Vii (Vineyards)**
*   **Livezi (Orchards)**
*   **Paduri (Forests)**
*   **Curti/Constructii (Yards/Buildings)**
*   **Suprafata (Area per category)**

## 3. Functional Requirements (Use Cases)
*   **UC-T01**: Declare total land surface owned by a household.
*   **UC-T02**: Break down land total surface into specific categories of use.
*   **UC-T03**: Register leased lands (Arenda) with contract details.
*   **UC-T04**: Update land surfaces due to acquisitions, sales, or category changes (e.g., pasture to arable).

## 4. Business Rules & Validations
*   **BR-01 (Surface Consistency)**: The sum of areas across all "Categorii de Folosinta" MUST equal the "Suprafata Totala" of the Teren entity.
*   **BR-02 (Lease Contracts)**: If Ownership Type is "Arenda", a valid lease contract number and duration must be provided.
*   **BR-03 (Intravilan vs Extravilan)**: Validations must ensure correct flagging of lands inside city limits (Intravilan) versus outside (Extravilan).

## 5. Integrations & Dependencies
*   **Parcele**: Terenuri are the macro level; Parcele are the micro (geospatial/cadastral) level.
*   **Carte Funciara**: Links total declared land to actual legal property registers.
*   **APIA**: Land data is frequently exported/checked against APIA (Agency for Payments and Intervention for Agriculture) for subsidies.
