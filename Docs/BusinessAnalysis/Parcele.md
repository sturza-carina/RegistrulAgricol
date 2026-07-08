# Business Analysis: Parcele (Land Parcels / Plots)

## 1. Overview
While "Terenuri" manages aggregate land areas per use category, "Parcele" (Parcels) handles the specific, individual plots of land. This module provides the granular, geospatial, and cadastral representation of the agricultural land.

## 2. Domain Entities & Attributes

### 2.1. Parcela (Parcel)
*   **ID Parcela**: Unique identifier.
*   **Teren ID**: Link to the parent land aggregate.
*   **Numar Bloc Fizic (Physical Block Number)**: APIA identifier.
*   **Numar Parcela Agricola (Agricultural Parcel Number)**
*   **Suprafata Parcela (Parcel Area)**: In hectares/square meters.
*   **Categorie Folosinta (Use Category)**: Specific use for this exact plot.
*   **Localizare Geografica (Geospatial Data)**: GPS Coordinates, Polygons (GeoJSON).
*   **Tarla (Tract)**: Topographic identifier.
*   **Parcela Cadastrala (Cadastral Parcel Number)**: Link to Cadastre.

## 3. Functional Requirements (Use Cases)
*   **UC-PA01**: Register a specific parcel with physical identifiers (Tarla, Parcela).
*   **UC-PA02**: Draw or upload spatial polygon data for the parcel.
*   **UC-PA03**: Split an existing parcel into two or more smaller parcels.
*   **UC-PA04**: Merge contiguous parcels of the same use category.
*   **UC-PA05**: Map parcels against APIA physical blocks for validation.

## 4. Business Rules & Validations
*   **BR-01 (Area Validation)**: The sum of areas of all parcels belonging to a specific "Categorie Folosinta" must equal the aggregate area declared in the "Terenuri" module.
*   **BR-02 (Unique Identification)**: The combination of Tarla, Parcela, and UAT must be unique.
*   **BR-03 (Spatial Overlap)**: Geospatial polygons for parcels belonging to the same household must not overlap.

## 5. Integrations & Dependencies
*   **Terenuri**: Parcels roll up into the macro Land module.
*   **Carte Funciara**: Requires the Numar Cadastral to integrate with the Land Registry.
*   **GIS / Maps**: Integration with mapping services (e.g., OpenStreetMap, ANCPI) for visualizing polygons.
