CREATE TABLE document (
    id BIGSERIAL PRIMARY KEY,
    gospodarie_id BIGINT NOT NULL REFERENCES gospodarii(id),
    tip_document_id INTEGER NOT NULL,
    nume_fisier VARCHAR(255) NOT NULL,
    cale_stocare VARCHAR(500) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    dimensiune_kb INTEGER,
    data_emitere DATE,
    data_expirare DATE,
    uploaded_by_id BIGINT,
    data_incarcare TIMESTAMP NOT NULL DEFAULT now(),
    observatii TEXT,
    este_activ BOOLEAN NOT NULL DEFAULT TRUE
);