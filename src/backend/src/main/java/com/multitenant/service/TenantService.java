package com.multitenant.service;

import com.multitenant.model.core.Tenant;
import com.multitenant.repository.TenantRepository;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.lang.NonNull;
import java.util.List;
import java.sql.Connection;
import java.sql.Statement;
import org.flywaydb.core.Flyway;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TenantService {

    private final TenantRepository tenantRepository;
    private final DataSource dataSource;

    public TenantService(TenantRepository tenantRepository, DataSource dataSource) {
        this.tenantRepository = tenantRepository;
        this.dataSource = dataSource;
    }

    public Tenant createTenant(String tenantId, String name) {
        if (tenantId == null || name == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant ID and name must not be null");
        }
        if (tenantRepository.existsById(tenantId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A tenant with ID '" + tenantId + "' already exists.");
        }

        String schemaName = "uat_" + tenantId;

        // Create schema and run Flyway migrations BEFORE saving to DB
        try {
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
            }
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .schemas(schemaName)
                    .locations("classpath:db/tenant")
                    .outOfOrder(false)
                    .validateOnMigrate(true)
                    .load();
            flyway.migrate();
        } catch (Exception e) {
            log.error("Flyway migration or schema creation failed for tenant: {}. Attempting schema cleanup...", schemaName, e);
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE");
                log.info("Successfully dropped orphaned schema: {}", schemaName);
            } catch (Exception dropEx) {
                log.error("Could not clean up schema after Flyway failure: {}", schemaName, dropEx);
            }
            throw new RuntimeException("Could not provision schema and run migrations for tenant: " + schemaName, e);
        }

        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        tenant.setName(name);
        tenant.setSchemaName(schemaName);

        // Save to public schema only after schema is successfully provisioned
        return tenantRepository.save(tenant);
    }

    public Tenant updateTenant(@NonNull String tenantId, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name must not be empty");
        }
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found"));
        tenant.setName(newName);
        return tenantRepository.save(tenant);
    }

    public void migrateAllTenants() {
        List<Tenant> tenants = tenantRepository.findAll();
        for (Tenant tenant : tenants) {
            try {
                try (Connection conn = dataSource.getConnection();
                     Statement stmt = conn.createStatement()) {
                    stmt.execute("CREATE SCHEMA IF NOT EXISTS " + tenant.getSchemaName());
                }
                Flyway flyway = Flyway.configure()
                        .dataSource(dataSource)
                        .schemas(tenant.getSchemaName())
                        .locations("classpath:db/tenant")
                        .outOfOrder(false)
                        .validateOnMigrate(true)
                        .load();
                flyway.migrate();
                System.out.println("Successfully migrated schema: " + tenant.getSchemaName());
            } catch (Exception e) {
                System.err.println("Could not run migrations for tenant: " + tenant.getSchemaName());
                e.printStackTrace();
            }
        }
    }
}
