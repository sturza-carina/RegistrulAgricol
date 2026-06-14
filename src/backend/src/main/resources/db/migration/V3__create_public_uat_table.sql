INSERT INTO public.tenants (id, name, schema_name) VALUES 
('cluj', 'Cluj-Napoca', 'uat_cluj'),
('bucuresti', 'Bucuresti', 'uat_bucuresti');

CREATE TABLE public.uat (
    id SERIAL PRIMARY KEY,
    cod_siruta VARCHAR(50) NOT NULL UNIQUE,
    denumire VARCHAR(255) NOT NULL,
    judet VARCHAR(100) NOT NULL,
    tip_uat VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id VARCHAR(255) REFERENCES public.tenants(id)
);

INSERT INTO public.uat (cod_siruta, denumire, judet, tip_uat, is_active, tenant_id) VALUES
('54975', 'Cluj-Napoca', 'Cluj', 'Municipiu', true, 'cluj'),
('55311', 'Florești', 'Cluj', 'Comună', true, 'cluj'),
('1017', 'București', 'București', 'Municipiu', true, 'bucuresti');
