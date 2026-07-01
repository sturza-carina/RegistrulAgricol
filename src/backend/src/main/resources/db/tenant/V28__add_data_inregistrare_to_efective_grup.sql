-- ============================================================
-- V28: Add missing data_inregistrare column to efective_grup
-- Purpose: The JPA entity EfectivGrup.java defines a required
--          (nullable = false) field 'dataInregistrare' mapped to
--          the column 'data_inregistrare', but no migration ever
--          created this column, causing:
--          "column eg1_0.data_inregistrare does not exist"
--          on every SELECT against efective_grup.
-- Why DEFAULT CURRENT_DATE: any existing rows in efective_grup
--   need a value to satisfy the NOT NULL constraint. Backfilling
--   with today's date is a reasonable default for pre-existing
--   records; the application always sends an explicit value on
--   new inserts (LocalDate.now() at entity instantiation).
-- ============================================================

ALTER TABLE efective_grup
    ADD COLUMN IF NOT EXISTS data_inregistrare DATE NOT NULL DEFAULT CURRENT_DATE;
