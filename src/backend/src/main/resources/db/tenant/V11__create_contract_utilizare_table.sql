CREATE TABLE IF NOT EXISTS contract_utilizare (
    id SERIAL PRIMARY KEY,
    teren_id INT NOT NULL REFERENCES teren(id) ON DELETE CASCADE,
    locator_proprietar_id INT REFERENCES public.users(id) ON DELETE SET NULL,
    locator_utilizator_id INT REFERENCES public.users(id) ON DELETE SET NULL,
    tip_contract VARCHAR(50) NOT NULL, -- ARENDA, COMODAT, CONCESIUNE, INCHIRIERE, ALTELE
    numar_contract VARCHAR(100) NOT NULL,
    data_semnare DATE,
    data_inceput DATE,
    data_sfarsit DATE,
    pret_arenda_ron_an DOUBLE PRECISION,
    pret_arenda_grau_kg_ha DOUBLE PRECISION,
    indexare_pret BOOLEAN DEFAULT FALSE,
    status_contract VARCHAR(50) DEFAULT 'ACTIV', -- ACTIV, EXPIRAT, REZILIAT, SUSPENDAT
    motiv_incetare TEXT,
    data_operare DATE DEFAULT CURRENT_DATE,
    utilizator_operare_id INT REFERENCES public.users(id) ON DELETE SET NULL,
    este_activ BOOLEAN DEFAULT TRUE
);
