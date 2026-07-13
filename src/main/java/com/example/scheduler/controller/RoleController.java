package com.example.scheduler.controller;

import com.example.scheduler.dto.RoleResponse;
import com.example.scheduler.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Available staff roles")
public class RoleController {
    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPCIONIST')")
    @Operation(summary = "GET /api/roles — list all available staff roles")
    public ResponseEntity<List<RoleResponse>> findAll() {
        return ResponseEntity.ok(roleService.findAll());
    }
}
