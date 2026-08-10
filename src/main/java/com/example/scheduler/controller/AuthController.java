package com.example.scheduler.controller;

import com.example.scheduler.dto.login.LoginRequest;
import com.example.scheduler.dto.login.LoginResponse;
import com.example.scheduler.dto.patient.PatientRegisterRequest;
import com.example.scheduler.dto.personal.PersonalRegisterRequest;
import com.example.scheduler.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Patient and staff authentication")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/patient/register")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RECEPTIONIST', 'DOCTOR')")
    @Operation(summary = "POST /api/auth/patient/register — register a new patient (administrator only)")
    public ResponseEntity<Void> registerPatient(@Valid @RequestBody PatientRegisterRequest request) {
        authService.registerPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/patient/login")
    @Operation(summary = "POST /api/auth/patient/login — authenticate a patient and return a JWT token")
    public ResponseEntity<LoginResponse> loginPatient(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.loginPatient(request));
    }

    @PostMapping("/personal/register")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "POST /api/auth/personal/register — register a new staff member (administrator only)")
    public ResponseEntity<Void> registerPersonal(@Valid @RequestBody PersonalRegisterRequest request) {
        authService.registerPersonal(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/personal/login")
    @Operation(summary = "POST /api/auth/personal/login — authenticate a staff member and return a JWT token")
    public ResponseEntity<LoginResponse> loginPersonal(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.loginPersonal(request));
    }
}
