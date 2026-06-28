ALTER TABLE contract_utilizare
    ADD COLUMN IF NOT EXISTS parcela_id INT REFERENCES parcele(id) ON DELETE SET NULL;
