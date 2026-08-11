package com.example.scheduler.controller;

import com.example.scheduler.dto.appointment.AppointmentRequest;
import com.example.scheduler.dto.appointment.AppointmentResponse;
import com.example.scheduler.dto.appointment.AppointmentSummaryItem;
import com.example.scheduler.enums.AppointmentStatus;
import com.example.scheduler.security.SecurityUtils;
import com.example.scheduler.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Book, confirm, cancel, and reschedule appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;

    @PostMapping
    @Operation(summary = "POST /api/appointments — book an appointment for a patient on a given schedule slot")
    public ResponseEntity<AppointmentResponse> book(@Valid @RequestBody AppointmentRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.book(request, Long.parseLong(auth.getName()), SecurityUtils.extractRole(auth)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "GET /api/appointments/{id} — get appointment details by ID")
    public ResponseEntity<AppointmentResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.findById(id));
    }

    @GetMapping
    @Operation(summary = "GET /api/appointments — list appointments, filtered by ?doctorId={id}&clientId={id}&status={status} (all optional; DOCTOR/PATIENT callers are scoped to themselves, RECEPTIONIST can filter freely or omit both for a clinic-wide list)")
    public ResponseEntity<Page<AppointmentResponse>> findAppointments(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) AppointmentStatus status,
            @PageableDefault(sort = "schedule.startTime", direction = Sort.Direction.ASC) Pageable pageable,
            Authentication auth
    ) {
        return ResponseEntity.ok(appointmentService.findAppointments(doctorId, clientId, status, pageable, Long.parseLong(auth.getName()), SecurityUtils.extractRole(auth)));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPTIONIST')")
    @Operation(summary = "PATCH /api/appointments/{id}/confirm — confirm a pending appointment")
    public ResponseEntity<AppointmentResponse> confirm(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(appointmentService.confirm(id, Long.parseLong(auth.getName()), SecurityUtils.extractRole(auth)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPTIONIST')")
    @Operation(summary = "PATCH /api/appointments/{id}/cancel — cancel an appointment, releases the slot back to AVAILABLE")
    public ResponseEntity<AppointmentResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.cancel(id));
    }

    @Data
    public static class RescheduleRequest { private Long scheduleId; }

    @PatchMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPTIONIST')")
    @Operation(summary = "PATCH /api/appointments/{id}/reschedule — move an appointment to a new schedule slot (body: {scheduleId})")
    public ResponseEntity<AppointmentResponse> reschedule(
            @PathVariable Long id,
            @RequestBody RescheduleRequest body) {
        return ResponseEntity.ok(appointmentService.reschedule(id, body.getScheduleId()));
    }

    @GetMapping("/board")
    @Operation(summary = "GET /api/appointments/board — appointments grouped by status, filtered by ?from={date}&to={date}&doctorId={id}&clientId={id} (doctorId/clientId optional; DOCTOR/PATIENT callers are scoped to themselves, RECEPTIONIST can filter freely or omit both for a clinic-wide view)")
    public ResponseEntity<Map<AppointmentStatus, List<AppointmentSummaryItem>>> getBoardByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long clientId,
            Authentication auth) {
        return ResponseEntity.ok(appointmentService.getBoardByRange(from, to, doctorId, clientId, Long.parseLong(auth.getName()), SecurityUtils.extractRole(auth)));
    }

    @GetMapping("/calendar")
    @Operation(summary = "GET /api/appointments/calendar — appointments grouped by day, filtered by ?month={month}&year={year}&doctorId={id}&clientId={id} (doctorId/clientId optional; DOCTOR/PATIENT callers are scoped to themselves, RECEPTIONIST can filter freely or omit both for a clinic-wide view)")
    public ResponseEntity<Map<String, List<AppointmentSummaryItem>>> getCalendar(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long clientId,
            Authentication auth) {
        return ResponseEntity.ok(appointmentService.getCalendar(month, year, doctorId, clientId, Long.parseLong(auth.getName()), SecurityUtils.extractRole(auth)));
    }
}
