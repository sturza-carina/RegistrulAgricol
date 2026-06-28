CREATE TABLE IF NOT EXISTS persoana_gospodarie (
    persoana_id BIGINT NOT NULL,
    gospodarie_id BIGINT NOT NULL,
    PRIMARY KEY (persoana_id, gospodarie_id),
    CONSTRAINT fk_pg_persoana FOREIGN KEY (persoana_id) REFERENCES persons(id) ON DELETE CASCADE,
    CONSTRAINT fk_pg_gospodarie FOREIGN KEY (gospodarie_id) REFERENCES gospodarie(id) ON DELETE CASCADE
);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'persons'
          AND column_name = 'gospodarie_id'
    ) THEN
        -- Execute dynamic SQL because parser might fail on missing column even inside IF
        EXECUTE 'INSERT INTO persoana_gospodarie (persoana_id, gospodarie_id) SELECT id, gospodarie_id FROM persons WHERE gospodarie_id IS NOT NULL';
        EXECUTE 'ALTER TABLE persons DROP COLUMN gospodarie_id';
    END IF;
END $$;
