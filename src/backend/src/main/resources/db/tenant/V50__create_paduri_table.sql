CREATE TABLE IF NOT EXISTS paduri (
    id SERIAL PRIMARY KEY,
    parcela_id BIGINT NOT NULL REFERENCES parcele(id) ON DELETE CASCADE,
    tip_vegetatie VARCHAR(100) NOT NULL,
    specie_predominanta VARCHAR(100),
    suprafata_ha DOUBLE PRECISION,
    an_plantare INTEGER,
    stare_vegetatie VARCHAR(50),
    observatii VARCHAR(500),
    deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    modified_at TIMESTAMP WITHOUT TIME ZONE,
    modified_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS paduri_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL,
    revtype SMALLINT,
    parcela_id BIGINT,
    tip_vegetatie VARCHAR(100),
    specie_predominanta VARCHAR(100),
    suprafata_ha DOUBLE PRECISION,
    an_plantare INTEGER,
    stare_vegetatie VARCHAR(50),
    observatii VARCHAR(500),
    deleted BOOLEAN,
    PRIMARY KEY (id, rev)
);
