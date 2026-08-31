package com.example.scheduler.service;

public interface SchemaProvisioningService {
    void createPublicSchema();
    void createTenantSchema(String schemaName);
}
