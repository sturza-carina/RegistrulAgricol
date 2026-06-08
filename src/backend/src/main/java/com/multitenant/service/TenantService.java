package com.multitenant.service;

import com.multitenant.model.Tenant;
import com.multitenant.repository.TenantRepository;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

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
        if (tenantRepository.existsById(sirutaCode)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A UAT with SIRUTA code " + sirutaCode + " already exists.");
        }

        String schemaName = "uat_" + sirutaCode;

        Tenant tenant = new Tenant();
        tenant.setId(schemaName);
        tenant.setName(name);
        tenant.setSchemaName(schemaName);
        
        // Save to public schema
        Tenant saved = tenantRepository.save(tenant);

        // Create schema and table natively
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // 1. Create schema
            statement.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
            
            // 2. Create users table
            String createTableSql = "CREATE TABLE IF NOT EXISTS " + schemaName + ".users (" +
                    "id SERIAL PRIMARY KEY, " +
                    "username VARCHAR(255) NOT NULL UNIQUE, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "role VARCHAR(50) NOT NULL, " +
                    "tenant_id VARCHAR(255)" +
                    ")";
            statement.execute(createTableSql);
            
        } catch (Exception e) {
            throw new RuntimeException("Could not provision schema and users table for UAT: " + schemaName, e);
        }

        return saved;
    }
}
