ALTER TABLE public.tenants RENAME COLUMN cod_siruta TO id;
ALTER TABLE public.tenants ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
