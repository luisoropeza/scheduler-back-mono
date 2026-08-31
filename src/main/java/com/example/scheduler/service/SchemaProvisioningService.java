package com.example.scheduler.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Service
@RequiredArgsConstructor
public class SchemaProvisioningService {

    private static final String PUBLIC_SCHEMA_DDL = "sql/public-schema.sql";
    private static final String TENANT_SCHEMA_DDL = "sql/tenant-schema.sql";

    private final DataSource dataSource;

    /**
     * Creates the shared tables (accounts, roles, clinics) in the public schema.
     * Runs at bean init so it completes before DataSeeder's first query and before
     * any tenant schema is provisioned — tenant-schema.sql has FKs into public.
     * Required because ddl-auto is 'none': Hibernate creates nothing on its own.
     */
    @PostConstruct
    public void createPublicSchema() {
        try {
            executeDdl(loadDdl(PUBLIC_SCHEMA_DDL));
        } catch (SQLException | IOException e) {
            throw new IllegalStateException("Failed to provision the public schema", e);
        }
    }

    public void createTenantSchema(String schemaName) {
        validateSchemaName(schemaName);
        try {
            String ddl = loadDdl(TENANT_SCHEMA_DDL).replace("{schema}", schemaName);
            executeDdl("CREATE SCHEMA IF NOT EXISTS " + schemaName + ";" + ddl);
        } catch (SQLException | IOException e) {
            throw new IllegalStateException("Failed to provision tenant schema: " + schemaName, e);
        }
    }

    private void executeDdl(String ddl) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(true);
            try (Statement stmt = conn.createStatement()) {
                for (String sql : ddl.split(";")) {
                    String trimmed = sql.strip();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
            }
        }
    }

    private String loadDdl(String resourcePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    private void validateSchemaName(String schemaName) {
        if (schemaName == null || !schemaName.matches("^clinic_\\d+$")) {
            throw new IllegalArgumentException("Invalid tenant schema name: " + schemaName);
        }
    }
}
