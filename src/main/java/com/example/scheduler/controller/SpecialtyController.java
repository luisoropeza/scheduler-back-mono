package com.example.scheduler.controller;

import com.example.scheduler.dto.SpecialtyResponse;
import com.example.scheduler.service.SpecialtyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/specialties")
@RequiredArgsConstructor
@Tag(name = "Specialties", description = "Available specialties")
public class SpecialtyController {
    private final SpecialtyService specialtyService;

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPCIONIST')")
    @Operation(summary = "GET /api/specialties — list all available specialties")
    public ResponseEntity<List<SpecialtyResponse>> findAll() {
        return ResponseEntity.ok(specialtyService.findAll());
    }
}
