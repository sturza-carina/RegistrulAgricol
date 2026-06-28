package com.multitenant.config.tenant;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class MultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider<String> {

    private static final String TENANT_ID_PATTERN = "^[a-zA-Z0-9_-]{1,32}$";

    private final DataSource dataSource;

    public MultiTenantConnectionProviderImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // validare prin regex
    // daca tenant ID-ul contine ceva in afara caracterelor a-z A-z 0-9 _ -, arunca SQLException inainte sa ajunga la concatenare
    private String resolveSchemaName(String tenantIdentifier) throws SQLException {
        if (tenantIdentifier == null || "public".equals(tenantIdentifier)) {
            return "public";
        }
        if (!tenantIdentifier.matches(TENANT_ID_PATTERN)) {
            throw new SQLException("Invalid tenant identifier: " + tenantIdentifier);
        }
        return "tenant_" + tenantIdentifier;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        Connection connection = getAnyConnection();
        String schemaName = resolveSchemaName(tenantIdentifier);
        try {
            connection.createStatement().execute(
                    "SET search_path TO \"" + schemaName + "\", public"
            );
        } catch (SQLException e) {
            throw new SQLException("Could not alter schema to " + schemaName, e);
        }
        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        try {
            connection.createStatement().execute("SET search_path TO public");
        } catch (SQLException e) {
            throw new SQLException("Could not reset schema to public", e);
        }
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }
}