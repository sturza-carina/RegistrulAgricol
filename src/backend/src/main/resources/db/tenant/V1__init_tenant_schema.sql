CREATE TABLE uat (
    id SERIAL PRIMARY KEY,
    cod_siruta VARCHAR(50) NOT NULL,
    denumire VARCHAR(255) NOT NULL,
    judet VARCHAR(100) NOT NULL,
    tip_uat VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL, -- ADMIN or USER
    tenant_id VARCHAR(255), -- Stores the tenant this schema belongs to
    nume VARCHAR(255),
    email VARCHAR(255),
    activ BOOLEAN DEFAULT TRUE,
    uat_id INTEGER REFERENCES uat(id)
);

CREATE TABLE gospodarie (
    id SERIAL PRIMARY KEY,
    cod_gospodarie VARCHAR(100) NOT NULL,
    adresa VARCHAR(255) NOT NULL,
    tip_gospodarie VARCHAR(50) NOT NULL,
    activa BOOLEAN DEFAULT TRUE,
    uat_id INTEGER REFERENCES uat(id)
);
