-- V31: Extindere tabel parcele cu campuri noi necesare pentru CarteFunciara
-- numar_cadastral: numarul cadastral unic al parcelei (completat de operator sau sincronizat cu ANCPI)
-- tip_zona: enum INTRAVILAN/EXTRAVILAN — critic pentru calculul impozitelor locale (DITL)
-- titular_drept_folosinta: persoana/entitatea care detine dreptul de folosinta al parcelei

ALTER TABLE parcele
    ADD COLUMN IF NOT EXISTS numar_cadastral        VARCHAR(100),
    ADD COLUMN IF NOT EXISTS tip_zona               VARCHAR(20)
        CHECK (tip_zona IN ('INTRAVILAN', 'EXTRAVILAN')),
    ADD COLUMN IF NOT EXISTS titular_drept_folosinta VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_parcele_numar_cadastral ON parcele(numar_cadastral);
