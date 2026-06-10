ALTER TABLE public.uat ADD COLUMN tenant_id VARCHAR(255);
ALTER TABLE public.uat ADD CONSTRAINT fk_uat_tenant FOREIGN KEY (tenant_id) REFERENCES public.tenants(id);
