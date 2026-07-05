CREATE TABLE specii_pomi (
    id BIGSERIAL PRIMARY KEY,
    denumire VARCHAR(100) NOT NULL UNIQUE,
    categorie_folosinta VARCHAR(50) -- optional: leaga specia de Livadă/Vii/Padure
);

INSERT INTO specii_pomi (denumire, categorie_folosinta) VALUES
('Măr', 'Livadă'),
('Păr', 'Livadă'),
('Prun', 'Livadă'),
('Nuc', 'Livadă'),
('Cireș', 'Livadă'),
('Vișin', 'Livadă'),
('Cais', 'Livadă'),
('Piersic', 'Livadă'),
('Gutuie', 'Livadă'),
('Viță de vie', 'Vii'),
('Stejar', 'Pădure'),
('Fag', 'Pădure'),
('Brad', 'Pădure'),
('Molid', 'Pădure'),
('Salcâm', 'Pădure'),
('Pin', 'Pădure');