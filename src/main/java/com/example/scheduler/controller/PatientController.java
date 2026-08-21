package com.example.scheduler.controller;

import com.example.scheduler.dto.patient.PatientRequest;
import com.example.scheduler.dto.patient.PatientResponse;
import com.example.scheduler.dto.personal.PersonalResponse;
import com.example.scheduler.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Tag(name = "Patients", description = "Manage patients")
public class PatientController {
    private final PatientService patientService;

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPTIONIST')")
    @Operation(summary = "GET /api/patients — list all patients")
    public ResponseEntity<Page<PatientResponse>> findAllPatients(
            @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(patientService.findAllPatients(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPTIONIST')")
    @Operation(summary = "GET /api/patients/{id} — get a patient by ID")
    public ResponseEntity<PatientResponse> findPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.findPatientById(id));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('PATIENT')")
    @Operation(summary = "PUT /api/patients/{id} — updatePatientById patient information")
    public ResponseEntity<PatientResponse> updatePatientProfile(@Valid @RequestBody PatientRequest request, Authentication auth) {
        return ResponseEntity.ok(patientService.updatePatientById(Long.parseLong(auth.getName()), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPTIONIST')")
    @Operation(summary = "PUT /api/patients/{id} — updatePatientById patient information")
    public ResponseEntity<PatientResponse> updatePatientById(@PathVariable Long id, @Valid @RequestBody PatientRequest request) {
        return ResponseEntity.ok(patientService.updatePatientById(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPTIONIST')")
    @Operation(summary = "DELETE /api/patients/{id} — deactivatePatientById (soft-deleteSchedule) a patient account")
    public ResponseEntity<Void> deactivatePatientById(@PathVariable Long id) {
        patientService.deactivatePatientById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{patientId}/doctors")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPTIONIST')")
    @Operation(summary = "GET /api/patients/{patientId}/doctors — list all doctors assigned to a patient")
    public ResponseEntity<List<PersonalResponse>> getDoctors(@PathVariable Long patientId) {
        return ResponseEntity.ok(patientService.getDoctorsOfPatient(patientId));
    }
}
