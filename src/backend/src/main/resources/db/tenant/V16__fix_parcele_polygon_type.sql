-- Fix polygon column type from geometry to jsonb (consistent with teren.polygon)
-- Idempotent: ruleaza conversia doar daca coloana nu e deja jsonb,
-- si converteste geometria existenta in GeoJSON in loc sa o stearga.
DO $$
DECLARE
current_type text;
BEGIN
SELECT data_type INTO current_type
FROM information_schema.columns
WHERE table_schema = current_schema()
  AND table_name = 'parcele'
  AND column_name = 'polygon';

IF current_type IS DISTINCT FROM 'jsonb' THEN
ALTER TABLE parcele
ALTER COLUMN polygon TYPE jsonb
        USING ST_AsGeoJSON(polygon)::jsonb;
END IF;
END $$;