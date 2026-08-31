package com.example.scheduler.controller;

import com.example.scheduler.dto.schedule.ScheduleRequest;
import com.example.scheduler.dto.schedule.ScheduleResponse;
import com.example.scheduler.enums.ERole;
import com.example.scheduler.enums.ScheduleStatus;
import com.example.scheduler.security.SecurityUtils;
import com.example.scheduler.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedules", description = "Schedules Controller")
public class ScheduleController {
    private final ScheduleService scheduleService;

    @GetMapping
    @Operation(summary = "GET /api/schedules — list all schedules, filter by ?doctorId={doctorId}?specialtyId={specialtyId}?status={status}?after={after}")
    public ResponseEntity<Page<ScheduleResponse>> findAllSchedules(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long specialtyId,
            @RequestParam(required = false) ScheduleStatus status,
            @RequestParam(required = false) LocalDateTime after,
            @PageableDefault(sort = "startTime", direction = Sort.Direction.ASC) Pageable pageable,
            Authentication auth
    ) {
        if (SecurityUtils.extractRole(auth).equals(ERole.DOCTOR.name())) {
            return ResponseEntity.ok(scheduleService.findAllSchedules(Long.parseLong(auth.getName()), specialtyId, status, after, pageable));
        }
        return ResponseEntity.ok(scheduleService.findAllSchedules(doctorId, specialtyId, status, after, pageable));
    }

    @GetMapping("/{scheduleId}")
    @Operation(summary = "GET /api/schedules/{scheduleId} — get a schedule slot by id")
    public ResponseEntity<ScheduleResponse> findScheduleById(@PathVariable Long scheduleId, Authentication auth) {
        return ResponseEntity.ok(scheduleService.findScheduleById(scheduleId, Long.parseLong(auth.getName()), SecurityUtils.extractRole(auth)));
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "POST /api/personal/schedules — add a single available time slot for a doctor")
    public ResponseEntity<ScheduleResponse> createSchedule(@Valid @RequestBody ScheduleRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.createSchedule(Long.parseLong(auth.getName()), request));
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "POST /api/personal/schedules/batch — add multiple available time slots for a doctor")
    public ResponseEntity<List<ScheduleResponse>> createSchedulesBatch(@Valid @RequestBody List<ScheduleRequest> requests, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.createSchedulesBatch(Long.parseLong(auth.getName()), requests));
    }

    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "DELETE /api/personal/schedules/{scheduleId} — remove an available slot")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long scheduleId, Authentication auth) {
        scheduleService.deleteScheduleById(scheduleId, Long.parseLong(auth.getName()));
        return ResponseEntity.noContent().build();
    }
}
