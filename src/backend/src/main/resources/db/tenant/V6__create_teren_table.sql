CREATE TABLE teren (
    id SERIAL PRIMARY KEY,
    denumire VARCHAR(255) NOT NULL,
    gospodarie_id INT UNIQUE REFERENCES gospodarie(id) ON DELETE CASCADE
);

ALTER TABLE parcele
ADD COLUMN teren_id INT REFERENCES teren(id) ON DELETE CASCADE;

ALTER TABLE parcele
DROP COLUMN gospodarie_id;
