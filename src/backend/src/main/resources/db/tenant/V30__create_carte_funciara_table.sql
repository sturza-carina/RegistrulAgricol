-- V30: Creare tabel carti_funciare
-- Relatia este 1-to-1 cu terenuri (UNIQUE constraint pe teren_id).
-- numar_cf si numar_topografic sunt nullable initial (completate ulterior de operator).
-- suprafata_totala_intabulata este recalculata automat de CarteFunciaraEventListener
-- la fiecare adaugare de parcela noua pe teren.

CREATE TABLE IF NOT EXISTS carti_funciare (
    id                          BIGSERIAL PRIMARY KEY,
    teren_id                    BIGINT NOT NULL UNIQUE REFERENCES terenuri(id) ON DELETE CASCADE,
    numar_cf                    VARCHAR(100),
    numar_topografic            VARCHAR(100),
    suprafata_totala_intabulata DOUBLE PRECISION,
    created_at                  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_carti_funciare_teren_id ON carti_funciare(teren_id);
