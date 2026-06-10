-- Update gospodarie table to use embedded Adresa
ALTER TABLE gospodarie DROP COLUMN adresa;
ALTER TABLE gospodarie ADD COLUMN county VARCHAR(255);
ALTER TABLE gospodarie ADD COLUMN localitate VARCHAR(255);
ALTER TABLE gospodarie ADD COLUMN street VARCHAR(255);
ALTER TABLE gospodarie ADD COLUMN street_number VARCHAR(255);
ALTER TABLE gospodarie ADD COLUMN building VARCHAR(255);
ALTER TABLE gospodarie ADD COLUMN staircase VARCHAR(255);
ALTER TABLE gospodarie ADD COLUMN floor INTEGER;
ALTER TABLE gospodarie ADD COLUMN apartment_number INTEGER;
ALTER TABLE gospodarie ADD COLUMN postal_code VARCHAR(255);

