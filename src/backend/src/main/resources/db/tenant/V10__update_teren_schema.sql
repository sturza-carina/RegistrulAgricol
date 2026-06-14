-- Drop unique constraint on gospodarie_id to allow multiple terens per gospodarie
ALTER TABLE teren DROP CONSTRAINT IF EXISTS teren_gospodarie_id_key;

-- Add new columns for Teren mapping
ALTER TABLE teren ADD COLUMN IF NOT EXISTS tip_teren VARCHAR(100);
ALTER TABLE teren ADD COLUMN IF NOT EXISTS stereo70_coordinates TEXT;
ALTER TABLE teren ADD COLUMN IF NOT EXISTS polygon JSONB;
