package com.example.scheduler.controller;

import com.example.scheduler.dto.ClinicRequest;
import com.example.scheduler.dto.ClinicResponse;
import com.example.scheduler.service.ClinicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<ClinicResponse>> findAll() {
        return ResponseEntity.ok(clinicService.findAll());
    }

    @PostMapping
    @Operation(summary = "POST /api/clinics — create a new clinic")
    public ResponseEntity<ClinicResponse> create(@Valid @RequestBody ClinicRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clinicService.create(request));
    }
}
