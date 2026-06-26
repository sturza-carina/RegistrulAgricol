CREATE TABLE IF NOT EXISTS surse_apa (
    id                SERIAL PRIMARY KEY,
    parcela_id        INT             NOT NULL REFERENCES parcele(id) ON DELETE CASCADE,
    tip_sursa         VARCHAR(100),
    debit_mc_ora      DOUBLE PRECISION,
    stare_functionare BOOLEAN         NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_surse_apa_parcela_id
    ON surse_apa(parcela_id);
