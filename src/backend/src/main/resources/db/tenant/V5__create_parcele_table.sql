CREATE TABLE IF NOT EXISTS parcele (
    id SERIAL PRIMARY KEY,
    denumire VARCHAR(255) NOT NULL,
    suprafata DOUBLE PRECISION NOT NULL,
    categorie_folosinta VARCHAR(100),
    polygon JSONB,
    gospodarie_id INT REFERENCES gospodarie(id) ON DELETE CASCADE
);
