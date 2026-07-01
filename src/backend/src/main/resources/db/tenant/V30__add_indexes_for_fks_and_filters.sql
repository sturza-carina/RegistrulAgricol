-- Indexes for foreign-key columns and commonly filtered columns.
-- Skips columns already covered by a UNIQUE constraint or a prior migration's index:
-- uats.cod_siruta, persons.cnp/cui, surse_apa.parcela_id, evenimente_animale.animal_id,
-- animale_individuale.gospodarie_id + numar_crotal, efective_grup.gospodarie_id (leading column
-- of idx_efectiv_grup_gospodarie_id_specie).

-- gospodarii
CREATE INDEX IF NOT EXISTS idx_gospodarii_uat_id ON gospodarii(uat_id);
CREATE INDEX IF NOT EXISTS idx_gospodarii_cod_gospodarie ON gospodarii(cod_gospodarie);

-- terenuri (lost its unique constraint on gospodarie_id in V10)
CREATE INDEX IF NOT EXISTS idx_terenuri_gospodarie_id ON terenuri(gospodarie_id);

-- parcele
CREATE INDEX IF NOT EXISTS idx_parcele_teren_id ON parcele(teren_id);

-- categorii_folosinta
CREATE INDEX IF NOT EXISTS idx_categorii_folosinta_teren_id ON categorii_folosinta(teren_id);

-- cladiri
CREATE INDEX IF NOT EXISTS idx_cladiri_gospodarie_id ON cladiri(gospodarie_id);
CREATE INDEX IF NOT EXISTS idx_cladiri_teren_id ON cladiri(teren_id);

-- utilaje
CREATE INDEX IF NOT EXISTS idx_utilaje_gospodarie_id ON utilaje(gospodarie_id);

-- culturi_parcele
CREATE INDEX IF NOT EXISTS idx_culturi_parcele_parcela_id ON culturi_parcele(parcela_id);

-- contracte_utilizare
CREATE INDEX IF NOT EXISTS idx_contracte_utilizare_parcela_id ON contracte_utilizare(parcela_id);
CREATE INDEX IF NOT EXISTS idx_contracte_utilizare_locator_proprietar_id ON contracte_utilizare(locator_proprietar_id);
CREATE INDEX IF NOT EXISTS idx_contracte_utilizare_locator_utilizator_id ON contracte_utilizare(locator_utilizator_id);
CREATE INDEX IF NOT EXISTS idx_contracte_utilizare_status_data_sfarsit ON contracte_utilizare(status_contract, data_sfarsit);

-- documente
CREATE INDEX IF NOT EXISTS idx_documente_gospodarie_id ON documente(gospodarie_id);

-- animale_individuale
CREATE INDEX IF NOT EXISTS idx_animale_individuale_proprietar_id ON animale_individuale(proprietar_id);
CREATE INDEX IF NOT EXISTS idx_animale_individuale_tenant_id ON animale_individuale(tenant_id);

-- efective_grup
CREATE INDEX IF NOT EXISTS idx_efective_grup_proprietar_id ON efective_grup(proprietar_id);
CREATE INDEX IF NOT EXISTS idx_efective_grup_tenant_id ON efective_grup(tenant_id);

-- evenimente_animale
CREATE INDEX IF NOT EXISTS idx_evenimente_animale_tenant_id ON evenimente_animale(tenant_id);

-- persons (single-table inheritance for PersoanaFizica/PersoanaJuridica)
CREATE INDEX IF NOT EXISTS idx_persons_person_type ON persons(person_type);
CREATE INDEX IF NOT EXISTS idx_persons_tenant_id ON persons(tenant_id);

-- persoane_gospodarii (many-to-many join table; composite PK (persoana_id, gospodarie_id)
-- covers persoana_id lookups but not a lookup by gospodarie_id alone)
CREATE INDEX IF NOT EXISTS idx_persoane_gospodarii_gospodarie_id ON persoane_gospodarii(gospodarie_id);

-- identity_documents
CREATE INDEX IF NOT EXISTS idx_identity_documents_person_id ON identity_documents(person_id);

-- person_relations
CREATE INDEX IF NOT EXISTS idx_person_relations_person_id ON person_relations(person_id);
CREATE INDEX IF NOT EXISTS idx_person_relations_related_person_id ON person_relations(related_person_id);

-- uats (tenant-local)
CREATE INDEX IF NOT EXISTS idx_uats_judet ON uats(judet);
