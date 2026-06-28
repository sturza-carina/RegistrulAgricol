-- Drop foreign key because utilizator_operare_id should point to public.users(id), not the local tenant schema persons table
ALTER TABLE contract_utilizare DROP CONSTRAINT IF EXISTS contract_utilizare_utilizator_operare_id_fkey;
