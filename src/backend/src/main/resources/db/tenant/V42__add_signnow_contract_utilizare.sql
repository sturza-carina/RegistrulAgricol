ALTER TABLE contracte_utilizare
    ADD COLUMN IF NOT EXISTS signnow_document_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS signnow_status VARCHAR(30),
    ADD COLUMN IF NOT EXISTS signnow_trimis_la TIMESTAMP,
    ADD COLUMN IF NOT EXISTS signnow_email_semnatar VARCHAR(255);
