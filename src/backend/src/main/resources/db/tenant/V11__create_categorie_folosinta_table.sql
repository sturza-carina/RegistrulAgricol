CREATE TABLE IF NOT EXISTS categorie_folosinta (
    id SERIAL PRIMARY KEY,
    denumire VARCHAR(255) NOT NULL,
    descriere TEXT,
    teren_id INT NOT NULL REFERENCES teren(id) ON DELETE CASCADE
);
