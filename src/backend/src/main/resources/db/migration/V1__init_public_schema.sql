CREATE TABLE public.tenants (
    cod_siruta VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    schema_name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Super admin should not belong to a specific tenant, but let's put superadmins in public schema for simplicity.
CREATE TABLE public.users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL, -- SUPER_ADMIN
    tenant_id VARCHAR(255) REFERENCES public.tenants(cod_siruta) -- Nullable for SUPER_ADMIN
);

-- Insert default super admin (password: superadmin)
INSERT INTO public.users (username, password, role) 
VALUES ('superadmin', '$2a$10$2hlN1uQM/pBQJhU6vWousuBwLlnYCKcrwrLyFOgjNbVWhMiZgUvrq', 'ROLE_SUPER_ADMIN');
