# Agriculture Register v1.2

The **Agriculture Register** application is a multi-tenant web platform designed for the management of agricultural assets (Gospodării, Terenuri, Parcele) and citizen records (Persoane) across various Administrative Territorial Units (UATs) in Romania.

## Architecture & Multi-Tenancy

The application implements a robust **Schema-per-Tenant** multi-tenancy model using PostgreSQL:
- **Global Context (`public` schema):** Houses global system configuration, all registered Tenants, all UATs, and system-wide Users (e.g., `ROLE_SUPER_ADMIN`).
- **Tenant Context (`tenant_uuid` schema):** Each localized administrative zone (usually mapped to a county or a cluster of UATs) gets an isolated PostgreSQL schema containing its specific `Gospodarii`, `Terenuri`, `Parcele`, and `Persoane`.

The tenant context is resolved on every request via the `X-Tenant-ID` HTTP header, processed by a backend `TenantFilter`, and bound to a `ThreadLocal` context (`TenantContext`).

### Roles and Authorization
- **`ROLE_SUPER_ADMIN`**: Can create new Tenants, manage global UATs, create Tenant Admins, and **impersonate** specific UATs to view their localized data.
- **`ROLE_ADMIN`**: Belongs to a specific Tenant. Can manage the UATs assigned to that tenant, manage local users (`ROLE_USER`), and oversee all local agricultural data.
- **`ROLE_USER`**: Standard registry operator that performs CRUD operations on the agricultural entities.

## Key Domains & Entities

The backend code has been refactored into distinct cohesive modules:

### 1. Core Module
- `Tenant`: Represents an isolated database schema.
- `Uat`: Administrative Territorial Unit. Has a `codSiruta` and is explicitly linked to a `Tenant`.
- `User`: System user, linked to a Role and an optional UAT.

### 2. Registru Module
- `Gospodarie`: The primary agricultural unit (Household).
- `Teren`: A 1-to-1 extension of `Gospodarie` storing accumulated land information. Created automatically when a `Gospodarie` is seeded.
- `Parcela`: A 1-to-N geographic plot attached to a `Teren`. Contains PostGIS `Polygon` geometries mapped to EPSG:3857 for web mapping.

### 3. Persoana Module
- `PersoanaFizica`: Individual citizens with CNP, birthdate, etc.
- `PersoanaJuridica`: Corporate entities with CUI, registration numbers, etc.

## UI & Frontend Features

Built with Angular (Standalone Components), the frontend features a modern, dynamic dashboard with role-based navigation.

### Global Admin Dashboard
- **Tenant & UAT Management**: Super Admins can define new Tenants and UATs.
- **UAT Impersonation**: By clicking "Administrare UAT" on a global UAT, the Super Admin enters an impersonation state (triggering an orange warning banner). The UI updates to inject the UAT's specific `tenantId` into all subsequent requests, granting the Super Admin seamless access to the localized `Gospodarii` and `Users` tabs without navigating away from the global app instance.

### Tenant Dashboard
- **Persoane**: Full CRUD for physical and legal persons.
- **Gospodării**: Comprehensive registry tracking agricultural households and their corresponding `Teren` assignments.
- **Harta Parcele (GIS)**: Interactive geographic interface built with Leaflet. Extracts `Polygon` geometry from the backend (delivered as WKT text) and renders individual `Parcele` on an interactive map.

## Database Migrations
Flyway is utilized to execute SQL migrations seamlessly. It runs on the `public` schema upon application startup, and dynamically iterates over all existing Tenant schemas to keep the multi-tenant architecture perfectly synchronized.

## Notable Security Features
Backend Controllers actively block Super Admins from mistakenly attempting to fetch localized registry data (like `Gospodarii`) while operating in the default `"public"` tenant scope. These fail-safes prevent silent SQL relation errors and gracefully return empty sets until the Super Admin explicitly impersonates a UAT.
