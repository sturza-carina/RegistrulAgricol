CREATE TABLE pasuni_fanete (
    id BIGSERIAL PRIMARY KEY,
    tip_folosinta VARCHAR(20) NOT NULL,
    suprafata_ha DOUBLE PRECISION NOT NULL,
    specii_dominante VARCHAR(255),
    numar_animale_pasunat INTEGER,
    numar_cosiri_anuale INTEGER,
    productie_estimata_kg_ha DOUBLE PRECISION,
    stare_vegetatie VARCHAR(50),
    sistem_intretinere VARCHAR(100),
    sistem_irigare VARCHAR(100),
    observatii VARCHAR(500),
    parcela_id BIGINT NOT NULL,
    CONSTRAINT fk_pasuni_fanete_parcela FOREIGN KEY (parcela_id) REFERENCES parcele(id)
);

CREATE INDEX idx_pasuni_fanete_parcela ON pasuni_fanete(parcela_id);
