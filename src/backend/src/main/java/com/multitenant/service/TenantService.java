package com.multitenant.service;

import com.multitenant.model.Tenant;
import com.multitenant.repository.TenantRepository;
import org.flywaydb.core.Flyway;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final DataSource dataSource;

    public TenantService(TenantRepository tenantRepository, DataSource dataSource) {
        this.tenantRepository = tenantRepository;
        this.dataSource = dataSource;
    }

    public Tenant createTenant(String name) {
        return createTenant(null, name);
    }

    public Tenant createTenant(String id, String name) {
        String finalId = (id == null || id.trim().isEmpty()) 
                ? UUID.randomUUID().toString().replace("-", "").toLowerCase() 
                : id.trim().toLowerCase();

        if (tenantRepository.existsById(finalId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A tenant with ID " + finalId + " already exists.");
        }

        String schemaName = "tenant" + finalId;

        Tenant tenant = new Tenant();
        tenant.setId(finalId);
        tenant.setName(name);
        tenant.setSchemaName(schemaName);
        tenant.setActive(true);

        // Save to public schema metadata table
        Tenant saved = tenantRepository.save(tenant);

        // Natively create schema and run Flyway migrations on it
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // 1. Create the database schema
            statement.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
            
            // 2. Run Flyway migration programmatically on the new schema
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .schemas(schemaName)
                    .locations("classpath:db/tenant")
                    .load();
            flyway.migrate();
            
        } catch (Exception e) {
            throw new RuntimeException("Could not provision schema and run migrations for tenant: " + schemaName, e);
        }

        return saved;
    }

    public Tenant getTenantById(String id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found with ID: " + id));
    }

    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    public Tenant updateTenant(String id, String name, boolean isActive) {
        Tenant tenant = getTenantById(id);
        tenant.setName(name);
        tenant.setActive(isActive);
        return tenantRepository.save(tenant);
    }

    public void deleteTenant(String id) {
        Tenant tenant = getTenantById(id);
        String schemaName = tenant.getSchemaName();

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // 1. Delete associated users from the public.users table to prevent FK constraint violations
            statement.execute("DELETE FROM public.users WHERE tenant_id = '" + id + "'");
            
            // 2. Drop the tenant schema CASCADE
            statement.execute("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE");
            
        } catch (Exception e) {
            throw new RuntimeException("Error executing database clean up for tenant: " + id, e);
        }

        // 3. Delete metadata row in public.tenants
        tenantRepository.delete(tenant);
    }
}
