-- Link parcele to real categorie_folosinta rows instead of a free-text string
ALTER TABLE parcele ADD COLUMN IF NOT EXISTS categorie_folosinta_id INT REFERENCES categorie_folosinta(id) ON DELETE SET NULL;

-- Backfill: ensure a categorie_folosinta row exists (per teren) for every distinct string value in use
INSERT INTO categorie_folosinta (denumire, descriere, teren_id)
SELECT DISTINCT p.categorie_folosinta, NULL, p.teren_id
FROM parcele p
WHERE p.categorie_folosinta IS NOT NULL
  AND p.teren_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM categorie_folosinta cf
    WHERE cf.teren_id = p.teren_id AND cf.denumire = p.categorie_folosinta
  );

-- Point each parcela at the matching categorie_folosinta row
UPDATE parcele p
SET categorie_folosinta_id = cf.id
FROM categorie_folosinta cf
WHERE cf.teren_id = p.teren_id
  AND cf.denumire = p.categorie_folosinta
  AND p.categorie_folosinta_id IS NULL;

ALTER TABLE parcele DROP COLUMN IF EXISTS categorie_folosinta;
