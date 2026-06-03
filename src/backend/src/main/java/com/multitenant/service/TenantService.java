package com.multitenant.service;

import com.multitenant.model.Tenant;
import com.multitenant.repository.TenantRepository;
import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.UUID;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final DataSource dataSource;

    public TenantService(TenantRepository tenantRepository, DataSource dataSource) {
        this.tenantRepository = tenantRepository;
        this.dataSource = dataSource;
    }

    public Tenant createTenant(String name, String schemaName) {
        Tenant tenant = new Tenant();
        tenant.setId(UUID.randomUUID().toString());
        tenant.setName(name);
        tenant.setSchemaName(schemaName);
        
        // Save to public schema
        Tenant saved = tenantRepository.save(tenant);

        // Create schema
        try {
            dataSource.getConnection().createStatement().execute("CREATE SCHEMA " + schemaName);
        } catch (Exception e) {
            throw new RuntimeException("Could not create schema " + schemaName, e);
        }

        // Run migrations for the new schema
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schemaName)
                .locations("classpath:db/tenant") // Separate folder for tenant migrations
                .load();
        flyway.migrate();

        return saved;
    }
}
