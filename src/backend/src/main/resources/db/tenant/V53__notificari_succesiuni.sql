-- V53__notificari_succesiuni.sql
-- Adăugare coloane de stare deces în tabela persons (PhysicalPerson / PersoanaFizica) și persons_aud
ALTER TABLE persons ADD COLUMN IF NOT EXISTS este_decedat BOOLEAN DEFAULT FALSE;
ALTER TABLE persons ADD COLUMN IF NOT EXISTS data_decesului DATE;
ALTER TABLE persons ADD COLUMN IF NOT EXISTS numar_certificat_deces VARCHAR(255);

ALTER TABLE persons_aud ADD COLUMN IF NOT EXISTS este_decedat BOOLEAN DEFAULT FALSE;
ALTER TABLE persons_aud ADD COLUMN IF NOT EXISTS data_decesului DATE;
ALTER TABLE persons_aud ADD COLUMN IF NOT EXISTS numar_certificat_deces VARCHAR(255);

-- Crearea tabelei notificari_succesiuni
CREATE TABLE IF NOT EXISTS notificari_succesiuni (
    id BIGSERIAL PRIMARY KEY,
    defunct_id INTEGER NOT NULL,
    defunct_cnp_hash VARCHAR(64) NOT NULL,
    nume_notar_spn_bin VARCHAR(255),
    numar_adresa_oficiala VARCHAR(100),
    data_trimitere DATE,
    stadiu_notificare VARCHAR(50) NOT NULL, -- TRIMIS, IN_LUCRU, FINALIZAT
    observatii TEXT,
    utilizator_operare VARCHAR(255),
    data_inregistrare TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notificari_succesiuni_defunct FOREIGN KEY (defunct_id) REFERENCES persons(id) ON DELETE CASCADE
);

-- Adăugare index de performanță pe defunct_cnp_hash
CREATE INDEX IF NOT EXISTS idx_notificari_succesiuni_cnp_hash ON notificari_succesiuni(defunct_cnp_hash);
