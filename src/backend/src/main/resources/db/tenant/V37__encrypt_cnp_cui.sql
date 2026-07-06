-- Alter columns in persons to accommodate longer encrypted Base64 strings
ALTER TABLE persons ALTER COLUMN cnp TYPE VARCHAR(255);
ALTER TABLE persons ALTER COLUMN cui TYPE VARCHAR(255);

-- Alter columns in persons_aud
ALTER TABLE persons_aud ALTER COLUMN cnp TYPE VARCHAR(255);
ALTER TABLE persons_aud ALTER COLUMN cui TYPE VARCHAR(255);

-- Drop old unique constraints on raw fields as we are encrypting them with non-deterministic AES-256-CBC
ALTER TABLE persons DROP CONSTRAINT IF EXISTS persons_cnp_key;
ALTER TABLE persons DROP CONSTRAINT IF EXISTS persons_cui_key;

-- Add hash columns to persons
ALTER TABLE persons ADD COLUMN IF NOT EXISTS cnp_hash VARCHAR(64);
ALTER TABLE persons ADD COLUMN IF NOT EXISTS cui_hash VARCHAR(64);

-- Add hash columns to persons_aud
ALTER TABLE persons_aud ADD COLUMN IF NOT EXISTS cnp_hash VARCHAR(64);
ALTER TABLE persons_aud ADD COLUMN IF NOT EXISTS cui_hash VARCHAR(64);

-- Create indexes on hash columns for fast exact-match lookups
CREATE INDEX IF NOT EXISTS idx_persons_cnp_hash ON persons(cnp_hash);
CREATE INDEX IF NOT EXISTS idx_persons_cui_hash ON persons(cui_hash);

-- Enable pgcrypto to perform the historical hashing directly in SQL
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Migrate existing cleartext data to hash columns using SHA-256 and the fixed salt
UPDATE persons 
SET cnp_hash = encode(digest(concat(cnp, 'RegistruAgricolDeterministicSalt_2026'), 'sha256'), 'hex') 
WHERE cnp IS NOT NULL AND cnp_hash IS NULL;

UPDATE persons 
SET cui_hash = encode(digest(concat(cui, 'RegistruAgricolDeterministicSalt_2026'), 'sha256'), 'hex') 
WHERE cui IS NOT NULL AND cui_hash IS NULL;

UPDATE persons_aud 
SET cnp_hash = encode(digest(concat(cnp, 'RegistruAgricolDeterministicSalt_2026'), 'sha256'), 'hex') 
WHERE cnp IS NOT NULL AND cnp_hash IS NULL;

UPDATE persons_aud 
SET cui_hash = encode(digest(concat(cui, 'RegistruAgricolDeterministicSalt_2026'), 'sha256'), 'hex') 
WHERE cui IS NOT NULL AND cui_hash IS NULL;
