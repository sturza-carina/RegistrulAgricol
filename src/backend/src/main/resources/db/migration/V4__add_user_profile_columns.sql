ALTER TABLE public.users ADD COLUMN IF NOT EXISTS nume VARCHAR(255);
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS email VARCHAR(255);
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS activ BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS uat_id INTEGER REFERENCES public.uat(id);

UPDATE public.users
SET nume = 'Super Admin', email = 'admin@registru.ro', activ = TRUE
WHERE username = 'superadmin' AND nume IS NULL;
