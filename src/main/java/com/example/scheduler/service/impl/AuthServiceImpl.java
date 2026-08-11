package com.example.scheduler.service.impl;

import com.example.scheduler.dto.login.LoginRequest;
import com.example.scheduler.dto.login.LoginResponse;
import com.example.scheduler.dto.patient.PatientRegisterRequest;
import com.example.scheduler.dto.personal.PersonalRegisterRequest;
import com.example.scheduler.entity.Account;
import com.example.scheduler.entity.Patient;
import com.example.scheduler.entity.Personal;
import com.example.scheduler.entity.Role;
import com.example.scheduler.entity.Specialty;
import com.example.scheduler.enums.ERole;
import com.example.scheduler.exception.BusinessException;
import com.example.scheduler.exception.ResourceNotFoundException;
import com.example.scheduler.exception.UnauthorizedException;
import com.example.scheduler.repository.AccountRepository;
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

    private final PatientRepository patientRepository;
    private final PersonalRepository personalRepository;
    private final AccountRepository accountRepository;
    private final SpecialtyRepository specialtyRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public void registerPatient(PatientRegisterRequest request) {
        Role patientRole = getOrCreateRole(ERole.PATIENT);

        Account account = accountRepository.findByEmail(request.getEmail())
                .map(existing -> {
                    if (existing.getRole().getName() != ERole.PATIENT)
                        throw new BusinessException("Email already registered");
                    if (!passwordEncoder.matches(request.getPassword(), existing.getPassword()))
                        throw new UnauthorizedException("Invalid credentials");
                    return existing;
                })
                .orElseGet(() -> accountRepository.save(Account.builder()
                        .name(request.getName())
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .phoneNumber(request.getPhoneNumber())
                        .role(patientRole)
                        .build()));

        if (patientRepository.findByAccount_Id(account.getId()).isPresent())
            throw new BusinessException("Already registered in this clinic");

        patientRepository.save(Patient.builder().account(account).build());
    }

    @Override
    public LoginResponse loginPatient(LoginRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail())
                .filter(a -> a.getRole().getName() == ERole.PATIENT)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        return new LoginResponse(jwtUtil.generate(account.getId(), account.getRole().getName().name()));
    }

    @Override
    @Transactional
    public void registerPersonal(PersonalRegisterRequest request) {
        Role role = getRoleOrThrow(request.getRoleId());

        Account account = accountRepository.findByEmail(request.getEmail())
                .map(existing -> {
                    if (existing.getRole().getName() == ERole.PATIENT)
                        throw new BusinessException("Email already registered");
                    if (!passwordEncoder.matches(request.getPassword(), existing.getPassword()))
                        throw new UnauthorizedException("Invalid credentials");
                    return existing;
                })
                .orElseGet(() -> accountRepository.save(Account.builder()
                        .name(request.getName())
                        .email(request.getEmail())
                        .role(role)
                        .password(passwordEncoder.encode(request.getPassword()))
                        .build()));

        if (personalRepository.findByAccount_Id(account.getId()).isPresent())
            throw new BusinessException("Already registered in this clinic");

        Personal personal = Personal.builder().account(account).build();
        if (request.getSpecialtyId() != null)
            if(role.getName() == ERole.DOCTOR)
                personal.setSpecialty(getSpecialtyOrThrow(request.getSpecialtyId()));
            else
                throw new BusinessException(String.format("this %s does not have a specialty assigned", role.getName()));
        personalRepository.save(personal);
    }

    @Override
    public LoginResponse loginPersonal(LoginRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail())
                .filter(a -> a.getRole().getName() != ERole.PATIENT)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        return new LoginResponse(jwtUtil.generate(account.getId(), account.getRole().getName().name()));
    }

    private Specialty getSpecialtyOrThrow(Long id) {
        return specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with id: " + id));
    }

    private Role getRoleOrThrow(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + id));
    }

    private Role getOrCreateRole(ERole name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(Role.builder().name(name).build()));
    }
}
