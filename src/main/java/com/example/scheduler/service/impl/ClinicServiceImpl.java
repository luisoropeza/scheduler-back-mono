package com.example.scheduler.service.impl;

import com.example.scheduler.config.tenant.TenantContext;
import com.example.scheduler.dto.ClinicRequest;
import com.example.scheduler.dto.ClinicResponse;
import com.example.scheduler.dto.PersonalRegisterRequest;
import com.example.scheduler.entity.Clinic;
import com.example.scheduler.entity.Role;
import com.example.scheduler.enums.ERole;
import com.example.scheduler.mapper.ClinicMapper;
import com.example.scheduler.repository.ClinicRepository;
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

    @Override
    @Transactional
    public ClinicResponse create(ClinicRequest request) {
        Clinic saved = clinicRepository.save(clinicMapper.toEntity(request));

        try {
            TenantContext.setCurrentTenant(saved.getId().toString());
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

        return clinicMapper.toResponse(saved);
    }

    @Override
    public List<ClinicResponse> findAll() {
        return clinicMapper.toResponseList(clinicRepository.findAll());
    }
}
