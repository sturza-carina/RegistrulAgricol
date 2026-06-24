package com.multitenant.repository;

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
                "SELECT nume FROM public.tip_sol ORDER BY nume", String.class);
    }

    public List<String> findAllCategoriiFolosinta() {
        return jdbcTemplate.queryForList(
                "SELECT nume FROM public.categorie_folosinta_ref ORDER BY nume", String.class);
    }

    public List<String> findAllTipuriSursaApa() {
        return jdbcTemplate.queryForList(
                "SELECT nume FROM public.tip_sursa_apa ORDER BY nume", String.class);
    }
}