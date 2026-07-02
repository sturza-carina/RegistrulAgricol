CREATE TABLE revinfo (
    rev SERIAL PRIMARY KEY,
    revtstmp BIGINT,
    username VARCHAR(255)
);

CREATE TABLE gospodarii_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL,
    revtype SMALLINT,
    cod_gospodarie VARCHAR(100),
    tip_gospodarie VARCHAR(50),
    activa BOOLEAN,
    uat_id INTEGER,
    county VARCHAR(255),
    localitate VARCHAR(255),
    street VARCHAR(255),
    street_number VARCHAR(255),
    building VARCHAR(255),
    staircase VARCHAR(255),
    floor INTEGER,
    apartment_number INTEGER,
    postal_code VARCHAR(255),
    PRIMARY KEY (id, rev),
    FOREIGN KEY (rev) REFERENCES revinfo(rev)
);

CREATE TABLE cladiri_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL,
    revtype SMALLINT,
    destinatie VARCHAR(255),
    suprafata_construita DOUBLE PRECISION,
    an_terminare INTEGER,
    materiale VARCHAR(255),
    adresa_sau_parcela VARCHAR(255),
    gospodarie_id BIGINT,
    teren_id BIGINT,
    PRIMARY KEY (id, rev),
    FOREIGN KEY (rev) REFERENCES revinfo(rev)
);

CREATE TABLE parcele_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL,
    revtype SMALLINT,
    denumire VARCHAR(255),
    suprafata DOUBLE PRECISION,
    categorie_folosinta VARCHAR(100),
    polygon JSONB,
    teren_id INTEGER,
    numar_cadastral VARCHAR(100),
    tip_zona VARCHAR(20),
    titular_drept_folosinta VARCHAR(255),
    PRIMARY KEY (id, rev),
    FOREIGN KEY (rev) REFERENCES revinfo(rev)
);
