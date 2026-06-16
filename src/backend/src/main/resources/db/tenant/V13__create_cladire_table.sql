CREATE TABLE cladiri (
    id BIGSERIAL PRIMARY KEY,
    destinatie VARCHAR(255) NOT NULL,
    suprafata_construita DOUBLE PRECISION NOT NULL,
    an_terminare INTEGER,
    materiale VARCHAR(255),
    adresa_sau_parcela VARCHAR(255),
    gospodarie_id BIGINT NOT NULL,
    teren_id BIGINT,
    CONSTRAINT fk_cladire_gospodarie FOREIGN KEY (gospodarie_id) REFERENCES gospodarie(id) ON DELETE CASCADE,
    CONSTRAINT fk_cladire_teren FOREIGN KEY (teren_id) REFERENCES teren(id) ON DELETE SET NULL
);
