TRUNCATE TABLE contracte_utilizare CASCADE;

ALTER TABLE contracte_utilizare DROP COLUMN IF EXISTS teren_id;
ALTER TABLE contracte_utilizare ADD COLUMN IF NOT EXISTS parcela_id INT;
ALTER TABLE contracte_utilizare ALTER COLUMN parcela_id SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_name = 'contracte_utilizare'
          AND constraint_name = 'contracte_utilizare_parcela_id_fkey'
    ) THEN
        ALTER TABLE contracte_utilizare
            ADD CONSTRAINT contracte_utilizare_parcela_id_fkey
            FOREIGN KEY (parcela_id) REFERENCES parcele(id) ON DELETE CASCADE;
    END IF;
END $$;
