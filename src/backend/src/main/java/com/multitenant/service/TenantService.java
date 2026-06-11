package com.multitenant.service;

import com.multitenant.model.core.Tenant;
import com.multitenant.repository.TenantRepository;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final DataSource dataSource;

    public TenantService(TenantRepository tenantRepository, DataSource dataSource) {
        this.tenantRepository = tenantRepository;
        this.dataSource = dataSource;
    }

    public Tenant createTenant(String sirutaCode, String name) {
        if (sirutaCode == null || name == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "SIRUTA code and name must not be null");
        }
        if (tenantRepository.existsById(sirutaCode)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A UAT with SIRUTA code " + sirutaCode + " already exists.");
        }

        String schemaName = "uat_" + sirutaCode;

        Tenant tenant = new Tenant();
        tenant.setId(sirutaCode);
        tenant.setName(name);
        tenant.setSchemaName(schemaName);

        // Save to public schema
        Tenant saved = tenantRepository.save(tenant);

        // Create schema and run flyway migrations for this new tenant
        try {
            org.flywaydb.core.Flyway flyway = org.flywaydb.core.Flyway.configure()
                    .dataSource(dataSource)
                    .schemas(schemaName)
                    .locations("classpath:db/tenant")
                    .load();
            flyway.migrate();
        } catch (Exception e) {
            throw new RuntimeException("Could not provision schema and run migrations for UAT: " + schemaName, e);
        }

        return saved;
    }

    public void migrateAllTenants() {
        java.util.List<Tenant> tenants = tenantRepository.findAll();
        for (Tenant tenant : tenants) {
            try {
                org.flywaydb.core.Flyway flyway = org.flywaydb.core.Flyway.configure()
                        .dataSource(dataSource)
                        .schemas(tenant.getSchemaName())
                        .locations("classpath:db/tenant")
                        .load();
                flyway.migrate();
                System.out.println("Successfully migrated schema: " + tenant.getSchemaName());
            } catch (Exception e) {
                System.err.println("Could not run migrations for UAT: " + tenant.getSchemaName());
                e.printStackTrace();
            }
        }
    }
}
