ALTER TABLE persons ADD COLUMN gospodarie_id INT REFERENCES gospodarie(id) ON DELETE SET NULL;
