CREATE TABLE public.tip_document (
    id SERIAL PRIMARY KEY,
    cod VARCHAR(50) NOT NULL UNIQUE,
    denumire VARCHAR(150) NOT NULL,
    descriere TEXT,
    activ BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO public.tip_document (cod, denumire) VALUES
('CONTRACT_ARENDA', 'Contract de arendă'),
('CONTRACT_CONCESIUNE', 'Contract de concesiune'),
('ADEVERINTA_PRIMARIE', 'Adeverință de la primărie'),
('DECLARATIE_PROPRIE_RASPUNDERE', 'Declarație pe propria răspundere'),
('ACT_PROPRIETATE', 'Act de proprietate'),
('CERTIFICAT', 'Certificat'),
('ALT_DOCUMENT', 'Alt document');