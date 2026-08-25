package com.example.scheduler.controller;

import com.example.scheduler.dto.patient.PatientResponse;
import com.example.scheduler.dto.personal.AssignAndRemoveRequest;
import com.example.scheduler.dto.personal.PersonalRequest;
import com.example.scheduler.dto.personal.PersonalResponse;
import com.example.scheduler.entity.Personal;
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

    @GetMapping("/doctors")
    @PreAuthorize("hasAnyRole('PATIENT', 'RECEPTIONIST')")
    @Operation(summary = "GET /api/personal/doctors — list all doctors, filter by ?specialtyId={specialtyId}?isActive={isActive}")
    public ResponseEntity<Page<PersonalResponse>> findAllDoctors(
            @RequestParam(required = false) Long specialtyId,
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(personalService.findAllDoctors(specialtyId, isActive, pageable));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR')")
    @Operation(summary = "GET /api/personal — list all personal, filter by ?specialtyId={specialtyId}&isActive={isActive}&role={role}")
    public ResponseEntity<Page<PersonalResponse>> findAllPersonal(
            @RequestParam(required = false) Long specialtyId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Long roleId,
            @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(personalService.findAllPersonal(specialtyId, isActive, roleId, pageable));
    }

    @GetMapping("/{personalId}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST, ADMINISTRATOR')")
    @Operation(summary = "GET /api/personal/{personalId} — get a personal by id")
    public ResponseEntity<PersonalResponse> findPersonalById(@PathVariable Long personalId) {
        return ResponseEntity.ok(personalService.findPersonalById(personalId));
    }

    @PutMapping("/update/{personalId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR')")
    @Operation(summary = "PUT /api/personal/{personalId} — update a personal by id")
    public ResponseEntity<PersonalResponse> updatePersonalById(@PathVariable Long personalId, @Valid @RequestBody PersonalRequest request) {
        return ResponseEntity.ok(personalService.updatePersonalById(personalId, request));
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('DOCTOR, RECEPTIONIST')")
    @Operation(summary = "PUT /api/personal — update self personal information")
    public ResponseEntity<PersonalResponse> updatePersonalProfile(@Valid @RequestBody PersonalRequest request, Authentication auth) {
        return ResponseEntity.ok(personalService.updatePersonalByAccountId(Long.parseLong(auth.getName()), request));
    }

    @DeleteMapping("deactivate/{personalId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR')")
    @Operation(summary = "DELETE /api/personal/{personalId} — deactivate a personal by id")
    public ResponseEntity<Void> deactivatePersonalById(@PathVariable Long personalId) {
        personalService.deactivatePersonalById(personalId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/patients/assign")
    @PreAuthorize("hasAnyRole('DOCTOR, RECEPTIONIST')")
    @Operation(summary = "POST /api/personal/patients/assign — assign a patient to a doctor")
    public ResponseEntity<Void> assignPatient(@Valid AssignAndRemoveRequest request, Authentication auth) {
        personalService.assignPatient(request, Long.parseLong(auth.getName()), SecurityUtils.extractRole(auth));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/patients/remove")
    @PreAuthorize("hasAnyRole('DOCTOR, RECEPTIONIST')")
    @Operation(summary = "DELETE /api/personal/patients/remove — remove a patient from a doctor")
    public ResponseEntity<Void> removePatient(@Valid AssignAndRemoveRequest request, Authentication auth) {
        personalService.removePatient(request, Long.parseLong(auth.getName()), SecurityUtils.extractRole(auth));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{doctorId}/patients")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPTIONIST')")
    @Operation(summary = "GET /api/personal/{doctorId}/patients — list all patients assigned to a doctor")
    public ResponseEntity<List<PatientResponse>> getPatients(@PathVariable Long doctorId) {
        return ResponseEntity.ok(personalService.getPatientsOfDoctor(doctorId));
    }
}
