-- ============================================================
-- V5: Global Crotal Registry (public schema)
-- Purpose: Enforce globally unique ear-tag (numar_crotal) IDs
--          across ALL tenant schemas, as required by SNIIA/ANSVSA.
-- Why here (public schema): The public schema is the only place
--   visible to every tenant. Each tenant's animal_individual table
--   enforces uniqueness within its own schema, but this table
--   acts as the cross-schema authority.
-- ============================================================

CREATE TABLE IF NOT EXISTS public.crotal_registry (
    numar_crotal  VARCHAR(100) PRIMARY KEY,   -- The globally unique ear-tag
    tenant_id     VARCHAR(255) NOT NULL,       -- Which UAT/schema currently "owns" this animal
    animal_id     BIGINT       NOT NULL,       -- Local ID in that tenant's schema (informational)
    inregistrat_la TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index to efficiently look up all crotals belonging to a tenant
CREATE INDEX IF NOT EXISTS idx_crotal_registry_tenant
    ON public.crotal_registry (tenant_id);
