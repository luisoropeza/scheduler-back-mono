package com.example.scheduler.service.impl;

import com.example.scheduler.config.tenant.TenantContext;
import com.example.scheduler.dto.clinic.ClinicRequest;
import com.example.scheduler.dto.clinic.ClinicResponse;
import com.example.scheduler.dto.personal.PersonalRegisterRequest;
import com.example.scheduler.entity.Clinic;
import com.example.scheduler.entity.PatientAccount;
import com.example.scheduler.entity.Role;
import com.example.scheduler.enums.ERole;
import com.example.scheduler.exception.ResourceNotFoundException;
import com.example.scheduler.mapper.ClinicMapper;
import com.example.scheduler.repository.ClinicRepository;
import com.example.scheduler.repository.PatientAccountRepository;
import com.example.scheduler.repository.PatientRepository;
import com.example.scheduler.repository.PersonalRepository;
import com.example.scheduler.repository.RoleRepository;
import com.example.scheduler.service.AuthService;
import com.example.scheduler.service.ClinicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClinicServiceImpl implements ClinicService {
    private final ClinicRepository clinicRepository;
    private final ClinicMapper clinicMapper;
    private final RoleRepository roleRepository;
    private final AuthService authService;
    private final PatientRepository patientRepository;
    private final PersonalRepository personalRepository;
    private final PatientAccountRepository patientAccountRepository;

    @Override
    @Transactional
    public ClinicResponse create(ClinicRequest request) {
        Clinic clinic = clinicRepository.save(clinicMapper.toEntity(request));
        try {
            TenantContext.setCurrentTenant(clinic.getId().toString());
            Role adminRole = roleRepository.findByName(ERole.ADMINISTRATOR.name())
                    .orElseGet(() -> roleRepository.save(Role.builder().name(ERole.ADMINISTRATOR.name()).build()));

            PersonalRegisterRequest adminRequest = new PersonalRegisterRequest();
            adminRequest.setName(request.getAdminName());
            adminRequest.setEmail(request.getAdminEmail());
            adminRequest.setPassword(request.getAdminPassword());
            adminRequest.setRoleId(adminRole.getId());
            authService.registerPersonal(adminRequest);
        } finally {
            TenantContext.clear();
        }

        return clinicMapper.toResponse(clinic);
    }

    @Override
    public List<ClinicResponse> findAll() {
        return clinicMapper.toResponseList(clinicRepository.findAll());
    }

    @Override
    public List<ClinicResponse> findByPatientAccountId(Long accountId) {
        return findByClinicIds(patientRepository.findClinicIdsByAccountId(accountId));
    }

    @Override
    public List<ClinicResponse> findByPersonalAccountId(Long accountId) {
        return findByClinicIds(personalRepository.findClinicIdsByAccountId(accountId));
    }

    @Override
    public List<ClinicResponse> findByPatientPhoneNumber(String phoneNumber) {
        PatientAccount account = patientAccountRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with phone number: " + phoneNumber));
        return findByPatientAccountId(account.getId());
    }

    private List<ClinicResponse> findByClinicIds(List<String> clinicIds) {
        List<Long> ids = clinicIds.stream().map(Long::valueOf).toList();
        return clinicMapper.toResponseList(clinicRepository.findAllById(ids));
    }
}
