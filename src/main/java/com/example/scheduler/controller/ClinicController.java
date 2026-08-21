package com.example.scheduler.controller;

import com.example.scheduler.dto.clinic.ClinicRequest;
import com.example.scheduler.dto.clinic.ClinicResponse;
import com.example.scheduler.enums.ERole;
import com.example.scheduler.security.SecurityUtils;
import com.example.scheduler.service.ClinicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinics")
@RequiredArgsConstructor
@Tag(name = "Clinics", description = "Create and retrieve clinics")
public class ClinicController {
    private final ClinicService clinicService;

    @GetMapping
    @Operation(summary = "GET /api/clinics — list all clinics")
    public ResponseEntity<List<ClinicResponse>> findAllClinics() {
        return ResponseEntity.ok(clinicService.findAllClinics());
    }

    @GetMapping("/mine")
    @Operation(summary = "GET /api/clinics/mine — list the clinics the authenticated user belongs to")
    public ResponseEntity<List<ClinicResponse>> findMineClinics(Authentication auth) {
        Long accountId = Long.parseLong(auth.getName());
        List<ClinicResponse> clinics = SecurityUtils.extractRole(auth).equals(ERole.PATIENT.name())
                ? clinicService.findClinicsByPatientAccountId(accountId)
                : clinicService.findClinicsByPersonalAccountId(accountId);
        return ResponseEntity.ok(clinics);
    }

    @PostMapping
    @Operation(summary = "POST /api/clinics — create clinic")
    public ResponseEntity<ClinicResponse> createClinic(@Valid @RequestBody ClinicRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clinicService.createClinic(request));
    }
}
