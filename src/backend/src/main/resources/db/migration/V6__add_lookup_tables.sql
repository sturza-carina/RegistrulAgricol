CREATE TABLE IF NOT EXISTS public.tip_sol (
        id   SERIAL PRIMARY KEY,
        nume VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO public.tip_sol (nume) VALUES
('Cernoziom'),
('Podzol'),
('Aluvial'),
('Nisipos'),
('Lutos'),
('Argilos'),
('Sărăturat'),
('Altul')
ON CONFLICT (nume) DO NOTHING;


CREATE TABLE IF NOT EXISTS public.categorie_folosinta_ref (
        id   SERIAL PRIMARY KEY,
        nume VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO public.categorie_folosinta_ref (nume) VALUES
('Arabil'),
('Pășune'),
('Fânețe'),
('Livadă'),
('Vii'),
('Pădure'),
('Ape'),
('Alte')
ON CONFLICT (nume) DO NOTHING;


CREATE TABLE IF NOT EXISTS public.tip_sursa_apa (
        id   SERIAL PRIMARY KEY,
        nume VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO public.tip_sursa_apa (nume) VALUES
('Puț forat'),
('Fântână'),
('Rețea irigații'),
('Râu / Canal'),
('Acumulare'),
('Lac'),
('Altul')
ON CONFLICT (nume) DO NOTHING;