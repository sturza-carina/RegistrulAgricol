CREATE TABLE persoana_gospodarie (
    persoana_id BIGINT NOT NULL,
    gospodarie_id BIGINT NOT NULL,
    PRIMARY KEY (persoana_id, gospodarie_id),
    CONSTRAINT fk_pg_persoana FOREIGN KEY (persoana_id) REFERENCES persons(id) ON DELETE CASCADE,
    CONSTRAINT fk_pg_gospodarie FOREIGN KEY (gospodarie_id) REFERENCES gospodarie(id) ON DELETE CASCADE
);

-- Migrate existing data
INSERT INTO persoana_gospodarie (persoana_id, gospodarie_id)
SELECT id, gospodarie_id FROM persons WHERE gospodarie_id IS NOT NULL;

-- Drop old column
ALTER TABLE persons DROP COLUMN gospodarie_id;
