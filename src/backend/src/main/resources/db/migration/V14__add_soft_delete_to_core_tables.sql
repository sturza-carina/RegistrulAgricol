ALTER TABLE cetateni ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE uats ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- tenants.schema_name
ALTER TABLE tenants DROP CONSTRAINT IF EXISTS tenants_schema_name_key;
CREATE UNIQUE INDEX IF NOT EXISTS uq_tenants_schema_name ON tenants(schema_name) WHERE deleted = false;

-- users.username
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_username_key;
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_username ON users(username) WHERE deleted = false;

-- cetateni.email
ALTER TABLE cetateni DROP CONSTRAINT IF EXISTS cetateni_email_key;
CREATE UNIQUE INDEX IF NOT EXISTS uq_cetateni_email ON cetateni(email) WHERE deleted = false;

-- uats.cod_siruta
ALTER TABLE uats DROP CONSTRAINT IF EXISTS uats_cod_siruta_key;
ALTER TABLE uats DROP CONSTRAINT IF EXISTS uat_cod_siruta_key;
CREATE UNIQUE INDEX IF NOT EXISTS uq_public_uats_cod_siruta ON uats(cod_siruta) WHERE deleted = false;
