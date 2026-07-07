-- V21_1 (recreate_contract_utilizare_if_missing) a fost adaugata in repo dupa ce V22 rulase deja
-- pe unele scheme de tenant; din cauza out-of-order=true a fost reaplicata mai tarziu si a
-- reintrodus constrangerea FK gresita pe utilizator_operare_id (vezi V22 pentru explicatia
-- initiala: coloana retine public.users(id), nu persons(id) local tenantului).
ALTER TABLE contracte_utilizare DROP CONSTRAINT IF EXISTS contract_utilizare_utilizator_operare_id_fkey;
ALTER TABLE contracte_utilizare DROP CONSTRAINT IF EXISTS contracte_utilizare_utilizator_operare_id_fkey;
