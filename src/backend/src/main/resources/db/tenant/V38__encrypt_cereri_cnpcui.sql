-- Alter cnp_cui column in cereri to accommodate the longer encrypted Base64 string
ALTER TABLE cereri ALTER COLUMN cnp_cui TYPE VARCHAR(255);

-- Add cnp_cui_hash column to cereri
ALTER TABLE cereri ADD COLUMN IF NOT EXISTS cnp_cui_hash VARCHAR(64);

-- Create index on the new hash column for fast exact-match lookups
CREATE INDEX IF NOT EXISTS idx_cereri_cnp_cui_hash ON cereri(cnp_cui_hash);

-- Enable pgcrypto to perform the historical hashing directly in SQL
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Migrate existing cleartext data to hash column using SHA-256 and the fixed salt
UPDATE cereri 
SET cnp_cui_hash = encode(digest(concat(cnp_cui, 'RegistruAgricolDeterministicSalt_2026'), 'sha256'), 'hex') 
WHERE cnp_cui IS NOT NULL AND cnp_cui_hash IS NULL;
