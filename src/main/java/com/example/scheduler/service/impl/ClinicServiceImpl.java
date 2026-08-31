package com.example.scheduler.service.impl;

import com.example.scheduler.config.tenant.TenantContext;
import com.example.scheduler.dto.clinic.ClinicCreatedResponse;
import com.example.scheduler.dto.clinic.ClinicRequest;
import com.example.scheduler.dto.clinic.ClinicResponse;
import com.example.scheduler.entity.Account;
import com.example.scheduler.entity.Clinic;
import com.example.scheduler.entity.Personal;
import com.example.scheduler.entity.Role;
import com.example.scheduler.enums.ERole;
import com.example.scheduler.exception.ResourceNotFoundException;
import com.example.scheduler.mapper.ClinicMapper;
import com.example.scheduler.repository.AccountRepository;
import com.example.scheduler.repository.ClinicRepository;
import com.example.scheduler.repository.PersonalRepository;
import com.example.scheduler.repository.RoleRepository;
import com.example.scheduler.service.ClinicService;
import com.example.scheduler.service.SchemaProvisioningService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClinicServiceImpl implements ClinicService {

    private final ClinicRepository clinicRepository;
    private final ClinicMapper clinicMapper;
    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final PersonalRepository personalRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SchemaProvisioningService schemaProvisioningService;
    private final PlatformTransactionManager transactionManager;

    @Override
    public ClinicCreatedResponse createClinic(ClinicRequest request) {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        // Tx 1: persist clinic in public schema
        Clinic clinic = tx.execute(_ -> clinicRepository.save(clinicMapper.toEntity(request)));

        // DDL: create schema + tenant tables (autocommit, outside any tx)
        String schemaName = "clinic_" + clinic.getId();
        schemaProvisioningService.createTenantSchema(schemaName);

        // Tx 2: create admin account (public) + admin personal (tenant schema).
        // The tenant must be set BEFORE the transaction opens: Hibernate resolves the
        // session's tenant identifier when the session starts, so setting it inside the
        // lambda binds the session to "public" and writes into the wrong schema.
        try {
            TenantContext.setCurrentTenant(schemaName);
            return tx.execute(_ -> {
                Role adminRole = roleRepository.findByName(ERole.ADMINISTRATOR)
                        .orElseThrow(() -> new ResourceNotFoundException("Role ADMINISTRATOR not found"));

                Account account = accountRepository.findByEmail(request.getAdminEmail())
                        .orElseGet(() -> accountRepository.save(Account.builder()
                                .name(request.getAdminName())
                                .email(request.getAdminEmail())
                                .ci(request.getCi())
                                .password(passwordEncoder.encode(request.getAdminPassword()))
                                .build()));

                Personal admin = personalRepository.save(Personal.builder()
                        .account(account)
                        .role(adminRole)
                        .build());

                return new ClinicCreatedResponse(
                        clinic.getId(),
                        clinic.getName(),
                        clinic.getPhoneNumber(),
                        admin.getId(),
                        account.getEmail());
            });
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public List<ClinicResponse> findAllClinics() {
        return clinicMapper.toResponseList(clinicRepository.findAll());
    }
}
