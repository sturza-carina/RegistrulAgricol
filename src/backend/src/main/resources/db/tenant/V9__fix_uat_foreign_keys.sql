ALTER TABLE users DROP CONSTRAINT IF EXISTS users_uat_id_fkey;
ALTER TABLE users ADD CONSTRAINT users_uat_id_fkey FOREIGN KEY (uat_id) REFERENCES public.uat(id);

ALTER TABLE gospodarie DROP CONSTRAINT IF EXISTS gospodarie_uat_id_fkey;
ALTER TABLE gospodarie ADD CONSTRAINT gospodarie_uat_id_fkey FOREIGN KEY (uat_id) REFERENCES public.uat(id);

