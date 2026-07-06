package com.multitenant.repository;

import com.multitenant.dto.SpecieRefDTO;
import com.multitenant.dto.TipDocumentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LookupRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<String> findAllTipuriSol() {
        return jdbcTemplate.queryForList(
                "SELECT nume FROM public.tipuri_sol ORDER BY nume", String.class);
    }

    public List<String> findAllCategoriiFolosinta() {
        return jdbcTemplate.queryForList(
                "SELECT nume FROM public.categorii_folosinta_ref ORDER BY nume", String.class);
    }

    public List<String> findAllTipuriSursaApa() {
        return jdbcTemplate.queryForList(
                "SELECT nume FROM public.tipuri_sursa_apa ORDER BY nume", String.class);
    }

    public List<TipDocumentDTO> findAllTipuriDocument() {
        return jdbcTemplate.query(
                "SELECT id, cod, denumire, descriere FROM public.tip_document WHERE activ = true ORDER BY denumire",
                (rs, rowNum) -> new TipDocumentDTO(
                        rs.getInt("id"),
                        rs.getString("cod"),
                        rs.getString("denumire"),
                        rs.getString("descriere")
                )
        );
    }

    public List<SpecieRefDTO> findAllSpeciiPomi() {
        return jdbcTemplate.query(
                "SELECT id, denumire AS nume, categorie_folosinta FROM public.specii_pomi ORDER BY denumire",
                (rs, rowNum) -> new SpecieRefDTO(
                        rs.getInt("id"),
                        rs.getString("nume"),
                        rs.getString("categorie_folosinta")
                )
        );
    }
}