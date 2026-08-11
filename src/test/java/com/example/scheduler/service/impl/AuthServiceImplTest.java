package com.example.scheduler.service.impl;

import com.example.scheduler.dto.login.LoginRequest;
import com.example.scheduler.dto.patient.PatientRegisterRequest;
import com.example.scheduler.dto.personal.PersonalRegisterRequest;
import com.example.scheduler.entity.Account;
import com.example.scheduler.entity.Role;
import com.example.scheduler.enums.ERole;
import com.example.scheduler.exception.BusinessException;
import com.example.scheduler.exception.UnauthorizedException;
import com.example.scheduler.repository.AccountRepository;
import com.example.scheduler.repository.PatientRepository;
import com.example.scheduler.repository.PersonalRepository;
import com.example.scheduler.repository.RoleRepository;
import com.example.scheduler.repository.SpecialtyRepository;
import com.example.scheduler.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private PersonalRepository personalRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private SpecialtyRepository specialtyRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private static Role role(ERole name) {
        return Role.builder().id(1L).name(name).build();
    }

    private static Account account(Role role) {
        return Account.builder().id(10L).name("Someone").email("someone@email.com").password("hash").role(role).build();
    }

    @Test
    void registerPatient_throws_whenEmailAlreadyRegisteredAsPersonal() {
        when(roleRepository.findByName(ERole.PATIENT)).thenReturn(Optional.of(role(ERole.PATIENT)));
        when(accountRepository.findByEmail("someone@email.com")).thenReturn(Optional.of(account(role(ERole.DOCTOR))));

        PatientRegisterRequest request = new PatientRegisterRequest();
        request.setEmail("someone@email.com");
        request.setName("Someone");
        request.setPassword("password123");

        assertThrows(BusinessException.class, () -> authService.registerPatient(request));
    }

    @Test
    void registerPersonal_throws_whenEmailAlreadyRegisteredAsPatient() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role(ERole.DOCTOR)));
        when(accountRepository.findByEmail("someone@email.com")).thenReturn(Optional.of(account(role(ERole.PATIENT))));

        PersonalRegisterRequest request = new PersonalRegisterRequest();
        request.setEmail("someone@email.com");
        request.setName("Someone");
        request.setPassword("password123");
        request.setRoleId(1L);

        assertThrows(BusinessException.class, () -> authService.registerPersonal(request));
    }

    @Test
    void registerPatient_reusesExistingAccount_whenSameRole() {
        Account existing = account(role(ERole.PATIENT));
        when(roleRepository.findByName(ERole.PATIENT)).thenReturn(Optional.of(role(ERole.PATIENT)));
        when(accountRepository.findByEmail("someone@email.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(patientRepository.findByAccount_Id(existing.getId())).thenReturn(Optional.empty());

        PatientRegisterRequest request = new PatientRegisterRequest();
        request.setEmail("someone@email.com");
        request.setName("Someone");
        request.setPassword("password123");

        authService.registerPatient(request);
    }

    @Test
    void loginPatient_throws_whenAccountIsPersonalRole() {
        when(accountRepository.findByEmail("someone@email.com")).thenReturn(Optional.of(account(role(ERole.DOCTOR))));

        LoginRequest request = new LoginRequest();
        request.setEmail("someone@email.com");
        request.setPassword("password123");

        assertThrows(UnauthorizedException.class, () -> authService.loginPatient(request));
    }

    @Test
    void loginPersonal_throws_whenAccountIsPatientRole() {
        when(accountRepository.findByEmail("someone@email.com")).thenReturn(Optional.of(account(role(ERole.PATIENT))));

        LoginRequest request = new LoginRequest();
        request.setEmail("someone@email.com");
        request.setPassword("password123");

        assertThrows(UnauthorizedException.class, () -> authService.loginPersonal(request));
    }

    @Test
    void loginPersonal_succeeds_forNonPatientRole() {
        Account account = account(role(ERole.DOCTOR));
        when(accountRepository.findByEmail("someone@email.com")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtUtil.generate(account.getId(), "DOCTOR")).thenReturn("token");

        LoginRequest request = new LoginRequest();
        request.setEmail("someone@email.com");
        request.setPassword("password123");

        assertEquals("token", authService.loginPersonal(request).getToken());
    }
}
