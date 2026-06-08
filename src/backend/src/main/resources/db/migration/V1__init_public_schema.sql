CREATE TABLE public.tenants (
    id VARCHAR(255) PRIMARY KEY,
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
    tenant_id VARCHAR(255) REFERENCES public.tenants(id), -- Nullable for SUPER_ADMIN
    nume VARCHAR(255),
    email VARCHAR(255),
    activ BOOLEAN DEFAULT TRUE,
    uat_id INTEGER
);

-- Insert default super admin (password: superadmin)
INSERT INTO public.users (username, password, role, nume, email, activ) 
VALUES ('superadmin', '$2a$10$wE1mG1h8/r5q9aK5/r6/GOCvU33f9m6m/G.s8uT0s8P9X00V2YmUa', 'ROLE_SUPER_ADMIN', 'Super Admin', 'admin@registru.ro', true);
