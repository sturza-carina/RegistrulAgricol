-- Alter cnp column in public.cetateni to accommodate the longer encrypted Base64 string
ALTER TABLE public.cetateni ALTER COLUMN cnp TYPE VARCHAR(255);

-- Drop old unique constraint on raw cnp field as we are encrypting it with non-deterministic AES-256
ALTER TABLE public.cetateni DROP CONSTRAINT IF EXISTS cetateni_cnp_key;

-- Add cnp_hash column to public.cetateni
ALTER TABLE public.cetateni ADD COLUMN IF NOT EXISTS cnp_hash VARCHAR(64);

-- Create index on the new hash column
CREATE INDEX IF NOT EXISTS idx_cetateni_cnp_hash ON public.cetateni(cnp_hash);

-- Enable pgcrypto for historical migration of existing plain text CNP data
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Migrate existing cleartext data to hash column using SHA-256 and the fixed salt
UPDATE public.cetateni 
SET cnp_hash = encode(digest(concat(cnp, 'RegistruAgricolDeterministicSalt_2026'), 'sha256'), 'hex') 
WHERE cnp IS NOT NULL AND cnp_hash IS NULL;
