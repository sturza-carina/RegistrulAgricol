CREATE TABLE IF NOT EXISTS utilaje (
    id SERIAL PRIMARY KEY,
    tip_utilaj VARCHAR(50) NOT NULL,
    marca VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    an_fabricatie INT,
    numar_inmatriculare VARCHAR(50),
    gospodarie_id INT NOT NULL REFERENCES gospodarie(id) ON DELETE CASCADE
);
