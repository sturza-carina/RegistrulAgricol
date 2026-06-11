# Gospodarii, Parcele, and GIS Map Documentation

This document provides an overview of the core entities (`Gospodarie`, `Teren`, and `Parcela`) and the GIS Mapping features within the Registru Agricol application.

## 1. Gospodarie CRUD

The `Gospodarie` (Household) is the fundamental unit in the Agricultural Register.

### Data Model
- **`codGospodarie`**: Unique identifier for the household.
- **`adresa`**: The physical address (embedded object).
- **`tipGospodarie`**: Enum representing the type of household.
- **`uat`**: The territorial administrative unit to which it belongs.
- **`activa`**: Boolean flag indicating if the household is currently active.

### Lifecycle & Teren Association
There is a strict 1:1 conceptual relationship between a `Gospodarie` and a `Teren` (Land). 
- **Creation**: Whenever a new `Gospodarie` is created via the `GospodarieService`, an empty `Teren` is **automatically generated** and associated with it. By default, this `Teren` contains no parcels.
- **Updates & Deletions**: Modifying a `Gospodarie` updates its base fields. Deleting a `Gospodarie` will cascade or require cleanup of its associated `Teren` and `Parcele`.

---

## 2. Parcele CRUD

A `Parcela` (Parcel/Plot) represents a specific geographical subdivision of a `Teren`. A single `Teren` can have multiple parcels (1:N relationship).

### Data Model
- **`denumire`**: Name or label of the parcel.
- **`suprafata`**: The surface area (in hectares/square meters depending on configuration).
- **`categorieFolosinta`**: The agricultural use category (e.g., arable, pasture, orchard).
- **`polygon`**: A PostGIS JTS `Polygon` object representing the geographical boundaries of the parcel.
- **Transient Fields**: The backend exposes `terenId` and `gospodarieName` as transient fields on the `Parcela` entity so the frontend can easily display contextual ownership data without making additional complex API calls.

### Operations
- Parcels are managed via `ParcelaService`. They can be fetched globally for the active tenant or filtered by a specific `Teren` ID.
- Strict null checks and validation are enforced before saving or updating parcels to ensure data integrity.

---

## 3. GIS Map Integration (Harta)

The GIS mapping interface is built using **Leaflet.js** and the **Leaflet-Draw** plugin on the frontend, interacting with PostGIS geometries on the backend.

### View Modes

The map component (`Harta Parcele`) operates in two distinct modes depending on the user's context:

#### A. Global View (Read-Only)
- **Trigger**: When the user navigates to the map tab *without* selecting a specific `Gospodarie`.
- **Behavior**: The map fetches and renders **all** parcels across the entire UAT/Tenant.
- **Interactivity**: Leaflet-Draw tools are **disabled**. The user cannot create or edit polygons.
- **Tooltips**: Hovering over any parcel on the map displays a dynamic tooltip showing the parcel's details (name, area, category) along with the **Name of the Gospodarie** that owns it.

#### B. Focused View (Read-Write)
- **Trigger**: When the user selects a specific `Gospodarie` and views its map.
- **Behavior**: The map fetches and renders only the parcels belonging to the `Teren` of the selected `Gospodarie`.
- **Interactivity**: Leaflet-Draw tools are **enabled**. The user can:
  - Draw new polygons.
  - Edit the vertices of existing polygons.
  - Delete polygons.
- **Data Flow**: When a polygon is drawn or modified, the frontend converts the Leaflet geometry into GeoJSON. This GeoJSON is sent to the backend, where it is deserialized into a JTS `Polygon` and saved to the PostgreSQL database.

### Technical Stack
- **Database**: PostgreSQL with PostGIS extension.
- **Backend Model**: `org.locationtech.jts.geom.Polygon` mapped via Hibernate Spatial.
- **Frontend Mapping**: Leaflet.js (`leaflet`), Leaflet Draw (`leaflet-draw`).
- **Data Format**: GeoJSON for API transport.
