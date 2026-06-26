-- Crează tabelul pentru istoricul evenimentelor unui animal individual.
-- Evenimentele sunt imutabile: se adaugă, nu se modifică/șterg direct.

CREATE TABLE IF NOT EXISTS eveniment_animal (
    id              SERIAL PRIMARY KEY,
    animal_id       INT NOT NULL REFERENCES animal_individual(id) ON DELETE CASCADE,
    tip_eveniment   VARCHAR(50) NOT NULL,
    data_eveniment  DATE NOT NULL,
    detalii         TEXT,
    tenant_id       VARCHAR(255)
);

-- Index pentru interogările de timeline (filtru + ordonare după dată)
CREATE INDEX IF NOT EXISTS idx_eveniment_animal_animal_id_data
    ON eveniment_animal (animal_id, data_eveniment DESC);
