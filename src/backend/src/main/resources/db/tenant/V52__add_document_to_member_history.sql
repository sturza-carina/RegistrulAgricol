-- Adăugare document asociat în tabela de istoric membri (certificat naștere, deces etc.)
ALTER TABLE istoric_membri_gospodarie ADD COLUMN IF NOT EXISTS document_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_istoric_document') THEN
        ALTER TABLE istoric_membri_gospodarie ADD CONSTRAINT fk_istoric_document FOREIGN KEY (document_id) REFERENCES documente(id) ON DELETE SET NULL;
    END IF;
END $$;

-- Adăugare document asociat în tabela de audit istoric membri pentru Envers
ALTER TABLE istoric_membri_gospodarie_aud ADD COLUMN IF NOT EXISTS document_id BIGINT;
