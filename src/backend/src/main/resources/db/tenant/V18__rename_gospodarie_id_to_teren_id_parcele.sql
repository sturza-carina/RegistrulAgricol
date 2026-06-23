DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'parcele'
          AND column_name = 'gospodarie_id'
    ) THEN
ALTER TABLE parcele RENAME COLUMN gospodarie_id TO teren_id;
END IF;
END $$;