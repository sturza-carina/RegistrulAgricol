TRUNCATE TABLE contracte_utilizare CASCADE;

ALTER TABLE contracte_utilizare DROP COLUMN IF EXISTS teren_id;
ALTER TABLE contracte_utilizare ADD COLUMN parcela_id INT NOT NULL REFERENCES parcele(id) ON DELETE CASCADE;
