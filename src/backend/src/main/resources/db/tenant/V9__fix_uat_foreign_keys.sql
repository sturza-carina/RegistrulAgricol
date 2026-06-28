-- gospodarie.uat_id now references the local tenant-schema uat table (not public.uat)
ALTER TABLE gospodarie DROP CONSTRAINT IF EXISTS gospodarie_uat_id_fkey;
ALTER TABLE gospodarie ADD CONSTRAINT gospodarie_uat_id_fkey FOREIGN KEY (uat_id) REFERENCES uat(id);
