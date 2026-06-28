CREATE TABLE public.tenants (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    schema_name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- All users (SUPER_ADMIN, ADMIN, USER) live in the public schema.
-- uat_id is a plain BIGINT (no FK) — the referenced UAT lives in the tenant schema, not public.
CREATE TABLE public.users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    tenant_id VARCHAR(255) REFERENCES public.tenants(id),
    nume VARCHAR(255),
    email VARCHAR(255),
    activ BOOLEAN DEFAULT TRUE,
    uat_id BIGINT
);

-- Insert default super admin (password: superadmin)
INSERT INTO public.users (username, password, role, nume, email, activ)
VALUES ('superadmin', '$2a$10$wE1mG1h8/r5q9aK5/r6/GOCvU33f9m6m/G.s8uT0s8P9X00V2YmUa', 'ROLE_SUPER_ADMIN', 'Super Admin', 'admin@registru.ro', true);
