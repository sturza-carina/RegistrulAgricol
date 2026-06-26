package com.multitenant.service;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Serviciu dedicat gestionării registrului global de crotale (public.crotal_registry).
 *
 * De ce JdbcTemplate și nu JPA?
 *   Tabela public.crotal_registry este în schema publică, nu în schema tenant-ului curent.
 *   Hibernate multi-tenant routing-ul setează automat schema la uat_{tenantId} pentru
 *   orice operație JPA, deci nu putem accesa tabelele publice prin EntityManager.
 *   JdbcTemplate folosește conexiunea DataSource brută, fără Hibernate routing, astfel
 *   operațiile ajung întotdeauna în schema public.
 *
 * Scenarii:
 *   - CREATE: la înregistrarea unui animal nou cu crotal → se rezervă crotalul global
 *   - TRANSFER: la un transfer cross-tenant → se actualizează tenant_id în registru
 *   - DELETE: la ștergerea unui animal → se eliberează crotalul din registru
 */
@Service
public class CrotalRegistryService {

    private final JdbcTemplate jdbcTemplate;

    public CrotalRegistryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Verifică dacă un crotal este deja înregistrat GLOBAL (în orice tenant).
     * Folosit înainte de a crea sau edita un animal cu crotal.
     *
     * @param numarCrotal crotalul de verificat
     * @param excludeAnimalId ID-ul local al animalului curent (null la create, non-null la update)
     *                        — pentru a exclude animalul propriu din verificare
     * @param currentTenantId tenantul curent — la update, un crotal existent în ACELAȘI tenant
     *                        și la ACELAȘI animal este permis
     */
    public void validateCrotalGlobalUnic(String numarCrotal, Long excludeAnimalId, String currentTenantId) {
        if (numarCrotal == null || numarCrotal.isBlank()) {
            return; // Crotalul este opțional — nu validăm dacă nu e furnizat
        }

        // Verificăm în tabela publică dacă crotalul există
        String sql = "SELECT tenant_id, animal_id FROM public.crotal_registry WHERE numar_crotal = ?";
        var rows = jdbcTemplate.queryForList(sql, numarCrotal);

        if (!rows.isEmpty()) {
            String ownerTenant = (String) rows.get(0).get("tenant_id");
            Long ownerId = ((Number) rows.get(0).get("animal_id")).longValue();

            // Permitem dacă e același animal din același tenant (update scenario)
            boolean eSiAnimalulCurent = ownerTenant.equals(currentTenantId)
                    && excludeAnimalId != null
                    && ownerId.equals(excludeAnimalId);

            if (!eSiAnimalulCurent) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Crotalul '" + numarCrotal + "' este deja înregistrat la nivel național " +
                        "(tenant: " + ownerTenant + "). Numerele de crotal SNIIA sunt unice global.");
            }
        }
    }

    /**
     * Rezervă crotalul în registrul global după salvarea cu succes a animalului.
     * Se apelează DUPĂ ce animalul a fost persistat (avem ID-ul lui).
     */
    public void rezervaCrotal(String numarCrotal, String tenantId, Long animalId) {
        if (numarCrotal == null || numarCrotal.isBlank()) return;

        jdbcTemplate.update(
            "INSERT INTO public.crotal_registry (numar_crotal, tenant_id, animal_id) " +
            "VALUES (?, ?, ?) " +
            "ON CONFLICT (numar_crotal) DO UPDATE SET tenant_id = EXCLUDED.tenant_id, animal_id = EXCLUDED.animal_id",
            numarCrotal, tenantId, animalId
        );
    }

    /**
     * Actualizează tenant-ul proprietar după un transfer cross-tenant.
     * Crotalul rămâne același (identitate SNIIA), doar proprietarul se schimbă.
     */
    public void transferaCrotal(String numarCrotal, String destinatarTenantId, Long newAnimalId) {
        if (numarCrotal == null || numarCrotal.isBlank()) return;

        int updated = jdbcTemplate.update(
            "UPDATE public.crotal_registry SET tenant_id = ?, animal_id = ? WHERE numar_crotal = ?",
            destinatarTenantId, newAnimalId, numarCrotal
        );

        if (updated == 0) {
            // Crotalul nu era înregistrat global — îl înregistrăm acum (recovery case)
            rezervaCrotal(numarCrotal, destinatarTenantId, newAnimalId);
        }
    }

    /**
     * Eliberează crotalul din registrul global când animalul este șters.
     * ATENȚIE: nu apelați la transfer (animalul e recreat în alt tenant).
     */
    public void elibereazaCrotal(String numarCrotal) {
        if (numarCrotal == null || numarCrotal.isBlank()) return;
        jdbcTemplate.update("DELETE FROM public.crotal_registry WHERE numar_crotal = ?", numarCrotal);
    }
}
