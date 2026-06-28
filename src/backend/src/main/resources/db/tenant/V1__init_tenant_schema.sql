-- UATs belong to this tenant and are stored locally in each tenant schema.
-- No FK back to public.tenants — they are co-located with their data.
CREATE TABLE uat (
    id SERIAL PRIMARY KEY,
    cod_siruta VARCHAR(50) NOT NULL UNIQUE,
    denumire VARCHAR(255) NOT NULL,
    judet VARCHAR(100) NOT NULL,
    tip_uat VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- NOTE: 'users' table is NOT created here. All users (SUPER_ADMIN, ADMIN, USER) live in public.users.

CREATE TABLE gospodarie (
    id SERIAL PRIMARY KEY,
    cod_gospodarie VARCHAR(100) NOT NULL,
    adresa VARCHAR(255) NOT NULL,
    tip_gospodarie VARCHAR(50) NOT NULL,
    activa BOOLEAN DEFAULT TRUE,
    uat_id INTEGER REFERENCES uat(id)
);
