package com.example.scheduler.service.impl;

import com.example.scheduler.dto.LoginRequest;
import com.example.scheduler.dto.LoginResponse;
import com.example.scheduler.dto.PatientRegisterRequest;
import com.example.scheduler.dto.PersonalRegisterRequest;
import com.example.scheduler.entity.Patient;
import com.example.scheduler.entity.Personal;
import com.example.scheduler.entity.Role;
import com.example.scheduler.entity.Specialty;
import com.example.scheduler.enums.ERole;
import com.example.scheduler.exception.BusinessException;
import com.example.scheduler.exception.ResourceNotFoundException;
import com.example.scheduler.mapper.PatientMapper;
import com.example.scheduler.mapper.PersonalMapper;
import com.example.scheduler.repository.PatientRepository;
import com.example.scheduler.repository.PersonalRepository;
import com.example.scheduler.repository.RoleRepository;
import com.example.scheduler.repository.SpecialtyRepository;
import com.example.scheduler.security.JwtUtil;
import com.example.scheduler.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    public static final String PATIENT_ROLE = "PATIENT";

    private final PatientRepository patientRepository;
    private final PersonalRepository personalRepository;
    private final SpecialtyRepository specialtyRepository;
    private final RoleRepository roleRepository;
    private final PatientMapper patientMapper;
    private final PersonalMapper personalMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public LoginResponse registerPatient(PatientRegisterRequest request) {
        if (patientRepository.findByEmail(request.getEmail()).isPresent() || personalRepository.findByEmail(request.getEmail()).isPresent())
            throw new BusinessException("Email already registered");
        Patient patient = patientMapper.toEntity(request);
        patient.setPassword(passwordEncoder.encode(request.getPassword()));
        return new LoginResponse(jwtUtil.generate(patientRepository.save(patient).getId(), PATIENT_ROLE));
    }

    @Override
    public LoginResponse loginPatient(LoginRequest request) {
        Patient patient = patientRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Invalid credentials"));
        if (!patient.isActive()) throw new BusinessException("Account is inactive");
        if (!passwordEncoder.matches(request.getPassword(), patient.getPassword())) {
            throw new BusinessException("Invalid credentials");
        }
        return new LoginResponse(jwtUtil.generate(patient.getId(), PATIENT_ROLE));
    }

    @Override
    @Transactional
    public LoginResponse registerPersonal(PersonalRegisterRequest request) {
        if (patientRepository.findByEmail(request.getEmail()).isPresent() || personalRepository.findByEmail(request.getEmail()).isPresent())
            throw new BusinessException("Email already registered");
        Personal personal = personalMapper.toEntity(request);
        Role role = getRoleOrThrow(request.getRoleId());
        if (request.getSpecialtyId() != null)
            if(role.getName().equals(ERole.DOCTOR.name()))
                personal.setSpecialty(getSpecialtyOrThrow(request.getSpecialtyId()));
            else
                throw new BusinessException(String.format("this %s does not have a specialty assigned", role.getName()));
        personal.setPassword(passwordEncoder.encode(request.getPassword()));
        personal.setRole(role);
        return new LoginResponse(jwtUtil.generate(personalRepository.save(personal).getId(), role.getName()));
    }

    @Override
    public LoginResponse loginPersonal(LoginRequest request) {
        Personal personal = personalRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Invalid credentials"));
        if (!personal.isActive()) throw new BusinessException("Account is inactive");
        if (!passwordEncoder.matches(request.getPassword(), personal.getPassword())) {
            throw new BusinessException("Invalid credentials");
        }
        return new LoginResponse(jwtUtil.generate(personal.getId(), personal.getRole().getName()));
    }

    private Specialty getSpecialtyOrThrow(Long id) {
        return specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with id: " + id));
    }

    private Role getRoleOrThrow(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + id));
    }
}
