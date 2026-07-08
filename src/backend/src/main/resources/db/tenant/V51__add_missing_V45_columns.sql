-- 1. Alter Table parcele and parcele_aud
ALTER TABLE parcele ADD COLUMN IF NOT EXISTS tip_mediu VARCHAR(30) DEFAULT 'CAMP_DESCHIS' NOT NULL;
ALTER TABLE parcele ADD COLUMN IF NOT EXISTS suprafata_utila_mp DOUBLE PRECISION;

ALTER TABLE parcele_aud ADD COLUMN IF NOT EXISTS tip_mediu VARCHAR(30);
ALTER TABLE parcele_aud ADD COLUMN IF NOT EXISTS suprafata_utila_mp DOUBLE PRECISION;

-- 2. Create Table cicluri_productie and cicluri_productie_aud
CREATE TABLE IF NOT EXISTS cicluri_productie (
    id BIGSERIAL PRIMARY KEY,
    parcela_id BIGINT NOT NULL REFERENCES parcele(id) ON DELETE CASCADE,
    cultura VARCHAR(100) NOT NULL,
    data_infiintare DATE NOT NULL,
    data_defisare DATE,
    status VARCHAR(20) NOT NULL,
    program_sprijin BOOLEAN NOT NULL DEFAULT FALSE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS cicluri_productie_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL REFERENCES revinfo(rev),
    revtype SMALLINT,
    parcela_id BIGINT,
    cultura VARCHAR(100),
    data_infiintare DATE,
    data_defisare DATE,
    status VARCHAR(20),
    program_sprijin BOOLEAN,
    deleted BOOLEAN,
    PRIMARY KEY (id, rev)
);

-- 3. Create Table factori_mediu (IoT ready, no Envers audit)
CREATE TABLE IF NOT EXISTS factori_mediu (
    id BIGSERIAL PRIMARY KEY,
    parcela_id BIGINT NOT NULL REFERENCES parcele(id) ON DELETE CASCADE,
    temperatura DOUBLE PRECISION,
    umiditate_relativa DOUBLE PRECISION,
    data_inregistrare TIMESTAMP NOT NULL
);

-- 4. Create Table recoltari and recoltari_aud
CREATE TABLE IF NOT EXISTS recoltari (
    id BIGSERIAL PRIMARY KEY,
    parcela_id BIGINT NOT NULL REFERENCES parcele(id) ON DELETE CASCADE,
    ciclu_productie_id BIGINT REFERENCES cicluri_productie(id) ON DELETE SET NULL,
    cultura VARCHAR(100) NOT NULL,
    data_recoltare DATE NOT NULL,
    cantitate_kg DOUBLE PRECISION NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS recoltari_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL REFERENCES revinfo(rev),
    revtype SMALLINT,
    parcela_id BIGINT,
    ciclu_productie_id BIGINT,
    cultura VARCHAR(100),
    data_recoltare DATE,
    cantitate_kg DOUBLE PRECISION,
    deleted BOOLEAN,
    PRIMARY KEY (id, rev)
);

-- 5. Add columns to tratamente_fitosanitare / tratamente_fitosanitare_aud
ALTER TABLE tratamente_fitosanitare ADD COLUMN IF NOT EXISTS ciclu_productie_id BIGINT REFERENCES cicluri_productie(id) ON DELETE SET NULL;
ALTER TABLE tratamente_fitosanitare ADD COLUMN IF NOT EXISTS unitate_masura_doza VARCHAR(50);
ALTER TABLE tratamente_fitosanitare ADD COLUMN IF NOT EXISTS data_lansarii DATE;
ALTER TABLE tratamente_fitosanitare ADD COLUMN IF NOT EXISTS numar_cutii_indivizi INTEGER;

ALTER TABLE tratamente_fitosanitare_aud ADD COLUMN IF NOT EXISTS ciclu_productie_id BIGINT;
ALTER TABLE tratamente_fitosanitare_aud ADD COLUMN IF NOT EXISTS unitate_masura_doza VARCHAR(50);
ALTER TABLE tratamente_fitosanitare_aud ADD COLUMN IF NOT EXISTS data_lansarii DATE;
ALTER TABLE tratamente_fitosanitare_aud ADD COLUMN IF NOT EXISTS numar_cutii_indivizi INTEGER;

-- 6. Add columns to fertilizari / fertilizari_aud
ALTER TABLE fertilizari ADD COLUMN IF NOT EXISTS ciclu_productie_id BIGINT REFERENCES cicluri_productie(id) ON DELETE SET NULL;
ALTER TABLE fertilizari_aud ADD COLUMN IF NOT EXISTS ciclu_productie_id BIGINT;
