-- Ștergere coloană redundantă is_head_of_household din tabela de persoane și audit Envers
ALTER TABLE persons DROP COLUMN IF EXISTS is_head_of_household;
ALTER TABLE persons_aud DROP COLUMN IF EXISTS is_head_of_household;

-- Adăugare cap de gospodărie în tabela gospodării (asociază o gospodărie cu un cap de familie)
ALTER TABLE gospodarii ADD COLUMN cap_gospodarie_id BIGINT;
ALTER TABLE gospodarii ADD CONSTRAINT fk_gospodarii_cap_gospodarie FOREIGN KEY (cap_gospodarie_id) REFERENCES persons(id) ON DELETE SET NULL;

-- Adăugare cap de gospodărie în tabela de audit gospodării pentru Envers
ALTER TABLE gospodarii_aud ADD COLUMN cap_gospodarie_id BIGINT;

-- Creare tabelă istoric membri pentru urmărirea evenimentelor de intrare/ieșire
CREATE TABLE istoric_membri_gospodarie (
    id BIGSERIAL PRIMARY KEY,
    gospodarie_id BIGINT NOT NULL,
    persoana_id BIGINT NOT NULL,
    tip_eveniment VARCHAR(50) NOT NULL, -- 'INTRARE_NASTERE', 'INTRARE_CASATORIE', 'INTRARE_MUTARE', 'IESIRE_DECES', 'IESIRE_DIVORT', 'IESIRE_MUTARE', 'ALTELE'
    data_eveniment DATE NOT NULL,
    observatii TEXT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_istoric_gospodarie FOREIGN KEY (gospodarie_id) REFERENCES gospodarii(id) ON DELETE CASCADE,
    CONSTRAINT fk_istoric_persoana FOREIGN KEY (persoana_id) REFERENCES persons(id) ON DELETE CASCADE
);

-- Creare tabelă audit istoric membri pentru Envers
CREATE TABLE istoric_membri_gospodarie_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL,
    revtype SMALLINT,
    gospodarie_id BIGINT,
    persoana_id BIGINT,
    tip_eveniment VARCHAR(50),
    data_eveniment DATE,
    observatii TEXT,
    deleted BOOLEAN,
    PRIMARY KEY (id, rev),
    FOREIGN KEY (rev) REFERENCES revinfo(rev)
);

