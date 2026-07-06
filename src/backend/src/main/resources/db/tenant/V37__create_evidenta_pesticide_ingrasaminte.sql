-- Table: catalog_ppp
CREATE TABLE IF NOT EXISTS catalog_ppp (
    id BIGSERIAL PRIMARY KEY,
    denumire_comerciala VARCHAR(255) NOT NULL,
    tip VARCHAR(100) NOT NULL,
    daunator_vizat VARCHAR(255),
    doza_omologata DOUBLE PRECISION NOT NULL,
    timp_pauza INTEGER NOT NULL
);

-- Table: catalog_ingrasaminte
CREATE TABLE IF NOT EXISTS catalog_ingrasaminte (
    id BIGSERIAL PRIMARY KEY,
    denumire VARCHAR(255) NOT NULL,
    tip VARCHAR(50) NOT NULL, -- Organic / Chimic
    procent_azot DOUBLE PRECISION NOT NULL DEFAULT 0,
    procent_fosfor DOUBLE PRECISION NOT NULL DEFAULT 0,
    procent_potasiu DOUBLE PRECISION NOT NULL DEFAULT 0
);

-- Table: tratamente_fitosanitare
CREATE TABLE IF NOT EXISTS tratamente_fitosanitare (
    id BIGSERIAL PRIMARY KEY,
    data_efectuarii TIMESTAMP NOT NULL,
    fenofaza VARCHAR(100) NOT NULL,
    parcela_id BIGINT NOT NULL REFERENCES parcele(id) ON DELETE CASCADE,
    agent_daunator VARCHAR(255) NOT NULL,
    catalog_ppp_id BIGINT NOT NULL REFERENCES catalog_ppp(id),
    doza_utilizata DOUBLE PRECISION NOT NULL,
    suprafata_tratata DOUBLE PRECISION NOT NULL,
    cantitate_totala DOUBLE PRECISION NOT NULL,
    responsabil VARCHAR(255) NOT NULL,
    semnatura_electronica TEXT,
    data_incepere_recoltare DATE,
    document_dare_consum VARCHAR(100),
    doza_depasita BOOLEAN NOT NULL DEFAULT FALSE,
    justificare_supradozaj TEXT
);

-- Table: fertilizari
CREATE TABLE IF NOT EXISTS fertilizari (
    id BIGSERIAL PRIMARY KEY,
    data_aplicarii DATE NOT NULL,
    parcela_id BIGINT NOT NULL REFERENCES parcele(id) ON DELETE CASCADE,
    catalog_ingrasaminte_id BIGINT NOT NULL REFERENCES catalog_ingrasaminte(id),
    cantitate_bruta DOUBLE PRECISION NOT NULL,
    unitate_masura VARCHAR(20) NOT NULL, -- kg/ha or tone/ha
    aport_azot DOUBLE PRECISION NOT NULL,
    aport_fosfor DOUBLE PRECISION NOT NULL,
    aport_potasiu DOUBLE PRECISION NOT NULL
);

-- Audit tables for Hibernate Envers:
CREATE TABLE IF NOT EXISTS catalog_ppp_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL REFERENCES revinfo(rev),
    revtype SMALLINT,
    denumire_comerciala VARCHAR(255),
    tip VARCHAR(100),
    daunator_vizat VARCHAR(255),
    doza_omologata DOUBLE PRECISION,
    timp_pauza INTEGER,
    PRIMARY KEY (id, rev)
);

CREATE TABLE IF NOT EXISTS catalog_ingrasaminte_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL REFERENCES revinfo(rev),
    revtype SMALLINT,
    denumire VARCHAR(255),
    tip VARCHAR(50),
    procent_azot DOUBLE PRECISION,
    procent_fosfor DOUBLE PRECISION,
    procent_potasiu DOUBLE PRECISION,
    PRIMARY KEY (id, rev)
);

CREATE TABLE IF NOT EXISTS tratamente_fitosanitare_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL REFERENCES revinfo(rev),
    revtype SMALLINT,
    data_efectuarii TIMESTAMP,
    fenofaza VARCHAR(100),
    parcela_id BIGINT,
    agent_daunator VARCHAR(255),
    catalog_ppp_id BIGINT,
    doza_utilizata DOUBLE PRECISION,
    suprafata_tratata DOUBLE PRECISION,
    cantitate_totala DOUBLE PRECISION,
    responsabil VARCHAR(255),
    semnatura_electronica TEXT,
    data_incepere_recoltare DATE,
    document_dare_consum VARCHAR(100),
    doza_depasita BOOLEAN,
    justificare_supradozaj TEXT,
    PRIMARY KEY (id, rev)
);

CREATE TABLE IF NOT EXISTS fertilizari_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL REFERENCES revinfo(rev),
    revtype SMALLINT,
    data_aplicarii DATE,
    parcela_id BIGINT,
    catalog_ingrasaminte_id BIGINT,
    cantitate_bruta DOUBLE PRECISION,
    unitate_masura VARCHAR(20),
    aport_azot DOUBLE PRECISION,
    aport_fosfor DOUBLE PRECISION,
    aport_potasiu DOUBLE PRECISION,
    PRIMARY KEY (id, rev)
);

-- Insert initial catalog entries
INSERT INTO catalog_ppp (denumire_comerciala, tip, daunator_vizat, doza_omologata, timp_pauza) VALUES
('Champ 77 WG', 'Fungicid', 'Mană, Rapan', 2.0, 7),
('Roundup Extra', 'Erbicid', 'Buruieni monocotiledonate și dicotiledonate', 4.0, 14),
('Decis 25 EC', 'Insecticid', 'Gândacul din Colorado, Afide', 0.5, 3),
('Mospilan 20 SG', 'Insecticid', 'Afide, Trips', 0.25, 7),
('Amistar', 'Fungicid', 'Făinare, Rugină', 0.75, 10);

INSERT INTO catalog_ingrasaminte (denumire, tip, procent_azot, procent_fosfor, procent_potasiu) VALUES
('Azotat de amoniu', 'Chimic', 34.0, 0.0, 0.0),
('Superfosfat', 'Chimic', 0.0, 20.0, 0.0),
('Complex NPK 15-15-15', 'Chimic', 15.0, 15.0, 15.0),
('Gunoi de grajd (Bovine)', 'Organic', 0.5, 0.25, 0.6),
('Gunoi de pasăre (Păsări)', 'Organic', 1.5, 1.2, 0.8),
('Uree', 'Chimic', 46.0, 0.0, 0.0);
