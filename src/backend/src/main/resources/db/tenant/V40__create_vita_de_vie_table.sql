CREATE TABLE vita_de_vie (
    id BIGSERIAL PRIMARY KEY,
    tip_inregistrare VARCHAR(20) NOT NULL,
    specie VARCHAR(100) NOT NULL,
    soi VARCHAR(100),
    an_plantare INTEGER,
    numar_vite INTEGER,
    suprafata_ha DOUBLE PRECISION,
    densitate_vite_ha INTEGER,
    stare_vita VARCHAR(50),
    sistem_intretinere VARCHAR(100),
    sistem_irigare VARCHAR(100),
    productie_estimata_kg DOUBLE PRECISION,
    observatii VARCHAR(500),
    parcela_id BIGINT NOT NULL,
    CONSTRAINT fk_vita_de_vie_parcela FOREIGN KEY (parcela_id) REFERENCES parcele(id)
);

CREATE INDEX idx_vita_de_vie_parcela ON vita_de_vie(parcela_id);
