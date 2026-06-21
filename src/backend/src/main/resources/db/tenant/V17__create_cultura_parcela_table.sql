CREATE TABLE IF NOT EXISTS cultura_parcela (
    id                    SERIAL PRIMARY KEY,
    parcela_id            INT NOT NULL REFERENCES parcele(id) ON DELETE CASCADE,
    an_agricol            INT NOT NULL,
    specie_cultura        VARCHAR(100) NOT NULL,
    suprafata_cultivata_ha DOUBLE PRECISION NOT NULL,
    data_insamantare      DATE,
    data_recoltare        DATE,
    productie_hectar_kg   DOUBLE PRECISION,
    productie_totala_tone DOUBLE PRECISION,
    sistem_irigare        VARCHAR(100),
    tip_sol               VARCHAR(100),
    cultura_precedenta    VARCHAR(100)
);
