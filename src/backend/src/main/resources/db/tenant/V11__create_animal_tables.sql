CREATE TABLE IF NOT EXISTS animal_individual (
    id SERIAL PRIMARY KEY,
    gospodarie_id INT NOT NULL REFERENCES gospodarie(id) ON DELETE CASCADE,
    proprietar_id INT NOT NULL REFERENCES persons(id) ON DELETE RESTRICT,
    numar_crotal VARCHAR(100),
    specie VARCHAR(50) NOT NULL,
    rasa VARCHAR(100),
    sex VARCHAR(20) NOT NULL,
    data_nastere DATE,
    greutate_kg DOUBLE PRECISION,
    stare_activa BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS efectiv_grup (
    id SERIAL PRIMARY KEY,
    gospodarie_id INT NOT NULL REFERENCES gospodarie(id) ON DELETE CASCADE,
    proprietar_id INT NOT NULL REFERENCES persons(id) ON DELETE RESTRICT,
    specie VARCHAR(50) NOT NULL,
    numar_capete_familii INT NOT NULL,
    detalii VARCHAR(255),
    tenant_id VARCHAR(255)
);
