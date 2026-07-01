-- ============================================================
-- V29: Add missing destinatar_tenant_id column to evenimente_animale
-- Purpose: The JPA entity EvenimentAnimal.java defines an optional
--          field 'destinatarTenantId' mapped to the column
--          'destinatar_tenant_id', used by CrossTenantTransferService
--          to know which tenant schema to copy an animal to on a
--          VANZARE (sale) event that triggers a cross-tenant transfer.
--          The original V15__create_eveniment_animal_table.sql never
--          created this column, causing:
--          "column e1_0.destinatar_tenant_id does not exist"
--          on every load of an animal's event history — which in turn
--          blocked cascading deletes of AnimalIndividual (the delete
--          transaction rolls back because loading the events
--          collection fails before the DELETE statements run).
-- Why nullable, no default: the entity has no `nullable = false` and
--   the field is explicitly documented as staying NULL for any event
--   type other than VANZARE. No backfill is needed since existing
--   rows correctly have no value for this column.
-- ============================================================

ALTER TABLE evenimente_animale
    ADD COLUMN IF NOT EXISTS destinatar_tenant_id VARCHAR(255);
