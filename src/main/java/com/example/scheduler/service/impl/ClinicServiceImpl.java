package com.example.scheduler.service.impl;

import com.example.scheduler.config.tenant.TenantContext;
import com.example.scheduler.dto.clinic.ClinicRequest;
import com.example.scheduler.dto.clinic.ClinicResponse;
import com.example.scheduler.entity.Account;
import com.example.scheduler.entity.Clinic;
import com.example.scheduler.entity.Personal;
import com.example.scheduler.entity.Role;
import com.example.scheduler.enums.ERole;
import com.example.scheduler.mapper.ClinicMapper;
import com.example.scheduler.repository.AccountRepository;
import com.example.scheduler.repository.ClinicRepository;
import com.example.scheduler.repository.PatientRepository;
import com.example.scheduler.repository.PersonalRepository;
import com.example.scheduler.repository.RoleRepository;
import com.example.scheduler.service.ClinicService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClinicServiceImpl implements ClinicService {
    private final ClinicRepository clinicRepository;
    private final ClinicMapper clinicMapper;
    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final PatientRepository patientRepository;
    private final PersonalRepository personalRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ClinicResponse createClinic(ClinicRequest request) {
        Clinic clinic = clinicRepository.save(clinicMapper.toEntity(request));
        try {
            TenantContext.setCurrentTenant(clinic.getId().toString());
            Role role = roleRepository.findByName(ERole.ADMINISTRATOR)
                    .orElseGet(() -> roleRepository.save(Role.builder().name(ERole.ADMINISTRATOR).build()));

            Account account = accountRepository.findByEmail(request.getAdminEmail())
                    .orElseGet(() -> accountRepository.save(Account.builder()
                            .name(request.getName())
                            .email(request.getAdminEmail())
                            .ci(request.getCi())
                            .password(passwordEncoder.encode(request.getAdminPassword()))
                            .build()));
            Personal personal = Personal.builder().account(account).role(role).build();
            personalRepository.save(personal);
        } finally {
            TenantContext.clear();
        }
        return clinicMapper.toResponse(clinic);
    }

    @Override
    public List<ClinicResponse> findAllClinics() {
        return clinicMapper.toResponseList(clinicRepository.findAll());
    }

    @Override
    public List<ClinicResponse> findClinicsByPatientPhoneNumber(String phoneNumber) {
        return findByClinicIds(patientRepository.findClinicIdsByPhoneNumber(phoneNumber));
    }

    private List<ClinicResponse> findByClinicIds(List<String> clinicIds) {
        List<Long> ids = clinicIds.stream().map(Long::valueOf).toList();
        return clinicMapper.toResponseList(clinicRepository.findAllById(ids));
    }
}
