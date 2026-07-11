package com.example.scheduler.controller;

import com.example.scheduler.dto.PatientResponse;
import com.example.scheduler.dto.PersonalRequest;
import com.example.scheduler.dto.PersonalResponse;
import com.example.scheduler.security.SecurityUtils;
import com.example.scheduler.service.PersonalService;
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
@RequestMapping("/api/personal")
@RequiredArgsConstructor
@Tag(name = "Personal", description = "Manage staff and doctor-patient relationships")
public class PersonalController {
    private final PersonalService personalService;

    @GetMapping
    @Operation(summary = "GET /api/personal — list staff members, filter by ?specialtyId={specialtyId}?isActive={isActive}")
    public ResponseEntity<Page<PersonalResponse>> findAll(
            @RequestParam(required = false) Long specialtyId,
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(personalService.findAll(specialtyId, isActive, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPCIONIST')")
    @Operation(summary = "GET /api/personal/{id} — get a staff member by ID")
    public ResponseEntity<PersonalResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(personalService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPCIONIST')")
    @Operation(summary = "PUT /api/personal/{id} — update name, email, role, or specialty of a staff member")
    public ResponseEntity<PersonalResponse> update(@PathVariable Long id, @Valid @RequestBody PersonalRequest request) {
        return ResponseEntity.ok(personalService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPCIONIST')")
    @Operation(summary = "DELETE /api/personal/{id} — deactivate (soft-delete) a staff member")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        personalService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{doctorId}/patients/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPCIONIST')")
    @Operation(summary = "POST /api/personal/{doctorId}/patients/{patientId} — assign a patient to a doctor")
    public ResponseEntity<Void> assignPatient(
            @PathVariable Long doctorId,
            @PathVariable Long patientId,
            Authentication auth
    ) {
        personalService.assignPatient(doctorId, patientId, Long.parseLong(auth.getName()), SecurityUtils.extractRole(auth));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{doctorId}/patients/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPCIONIST')")
    @Operation(summary = "DELETE /api/personal/{doctorId}/patients/{patientId} — remove a patient from a doctor")
    public ResponseEntity<Void> removePatient(
            @PathVariable Long doctorId,
            @PathVariable Long patientId,
            Authentication auth
    ) {
        personalService.removePatient(doctorId, patientId, Long.parseLong(auth.getName()), SecurityUtils.extractRole(auth));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{doctorId}/patients")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPCIONIST')")
    @Operation(summary = "GET /api/personal/{doctorId}/patients — list all patients assigned to a doctor")
    public ResponseEntity<List<PatientResponse>> getPatients(@PathVariable Long doctorId) {
        return ResponseEntity.ok(personalService.getPatientsOfDoctor(doctorId));
    }
}
