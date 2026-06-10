CREATE TABLE public.uat (
    id SERIAL PRIMARY KEY,
    cod_siruta VARCHAR(50) NOT NULL UNIQUE,
    denumire VARCHAR(255) NOT NULL,
    judet VARCHAR(100) NOT NULL,
    tip_uat VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO public.uat (cod_siruta, denumire, judet, tip_uat, is_active) VALUES
('54975', 'Cluj-Napoca', 'Cluj', 'Municipiu', true),
('55311', 'Florești', 'Cluj', 'Comună', true),
('1017', 'București', 'București', 'Municipiu', false);
