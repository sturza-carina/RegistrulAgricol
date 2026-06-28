-- Seed default tenants with new schema naming convention: tenant_<id>
INSERT INTO public.tenants (id, name, schema_name) VALUES
('cluj', 'Cluj', 'tenant_cluj'),
('bucuresti', 'Bucuresti', 'tenant_bucuresti');

-- NOTE: public.uat no longer exists.
-- Each tenant schema now has its own local 'uat' table created by the tenant Flyway migration.

