-- ============================================================
-- V13: Animal Module Improvements (tenant schema)
-- Addresses gaps identified against SNIIA/ANSVSA requirements:
--   1. Unique constraint on numar_crotal per tenant schema
--   2. Transfer metadata column on eveniment_animal
--   3. data_inregistrare on efectiv_grup (snapshot model)
--   4. Indexes for by-gospodarie queries
-- ============================================================

-- 1. Unique constraint on ear-tag within this tenant's schema.
--    Per-schema uniqueness is enforced here; cross-schema
--    uniqueness is enforced by public.crotal_registry.
--    We use a partial unique index to allow NULL crotals
--    (animals not yet tagged at birth) without constraint violations.
CREATE UNIQUE INDEX IF NOT EXISTS uq_animal_individual_numar_crotal
    ON animal_individual (numar_crotal)
    WHERE (stare_activa = TRUE AND numar_crotal IS NOT NULL);

-- 2. Structured destination tenant reference for transfer events.
--    Previously, transfer destination was encoded as free-text in
--    `detalii`. This column makes it machine-readable so the
--    CrossTenantTransferService can target the correct schema.
ALTER TABLE eveniment_animal
    ADD COLUMN IF NOT EXISTS destinatar_tenant_id VARCHAR(255);

-- 3. Registration date for group stock snapshots.
--    ANSVSA requires a dated record of livestock counts.
--    Existing rows are back-filled with CURRENT_DATE.
ALTER TABLE efectiv_grup
    ADD COLUMN IF NOT EXISTS data_inregistrare DATE NOT NULL DEFAULT CURRENT_DATE;

-- 4. Performance indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_animal_individual_gospodarie_id
    ON animal_individual (gospodarie_id);

CREATE INDEX IF NOT EXISTS idx_efectiv_grup_gospodarie_id_specie
    ON efectiv_grup (gospodarie_id, specie, data_inregistrare DESC);
