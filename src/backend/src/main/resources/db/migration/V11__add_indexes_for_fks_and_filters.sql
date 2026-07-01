-- Indexes for commonly filtered columns in the public schema.
-- uats.cod_siruta and users.username already have UNIQUE constraints (implicit indexes) — skipped.

CREATE INDEX IF NOT EXISTS idx_public_uats_tenant_id ON public.uats(tenant_id);
CREATE INDEX IF NOT EXISTS idx_public_uats_judet ON public.uats(judet);

CREATE INDEX IF NOT EXISTS idx_users_tenant_id ON public.users(tenant_id);
