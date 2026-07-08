-- semnat_de_utilizator_id retine public.users(id) (ca si utilizator_operare_id, vezi V22),
-- nu un id din tabela locala persons, deci nu primeste FK catre persons.
ALTER TABLE contracte_utilizare
    ADD COLUMN IF NOT EXISTS semnat_electronic BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS data_semnaturii_electronice TIMESTAMP,
    ADD COLUMN IF NOT EXISTS cale_document_semnat VARCHAR(500),
    ADD COLUMN IF NOT EXISTS hash_document_semnat VARCHAR(128),
    ADD COLUMN IF NOT EXISTS semnat_de_utilizator_id INT;
