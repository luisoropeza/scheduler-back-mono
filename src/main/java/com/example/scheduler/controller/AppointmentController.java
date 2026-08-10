package com.example.scheduler.controller;

import com.example.scheduler.dto.appintment.AppointmentBoardItem;
import com.example.scheduler.dto.appintment.AppointmentCalendarItem;
import com.example.scheduler.dto.appintment.AppointmentPriorityRequest;
import com.example.scheduler.dto.appintment.AppointmentRequest;
import com.example.scheduler.dto.appintment.AppointmentResponse;
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


    //-------------------------

    //-------------------------

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

    @GetMapping("/client/{clientId}")
    @Operation(summary = "GET /api/appointments/client/{clientId} — list all appointments for a patient")
    public ResponseEntity<Page<AppointmentResponse>> findByClient(
            @PathVariable Long clientId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication auth
    ) {
        return ResponseEntity.ok(appointmentService.findByClientId(clientId, pageable, Long.parseLong(auth.getName()), SecurityUtils.extractRole(auth)));
    }

    @GetMapping("/personal/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPTIONIST')")
    @Operation(summary = "GET /api/appointments/personal/{doctorId} — list all appointments for a personal, filter by ?status={status}")
    public ResponseEntity<Page<AppointmentResponse>> findByDoctorAndStatus(
            @PathVariable Long doctorId,
            @RequestParam(required = false) AppointmentStatus status,
            @PageableDefault(sort = "schedule.startTime", direction = Sort.Direction.ASC) Pageable pageable,
            Authentication auth
    ) {
        return ResponseEntity.ok(appointmentService.findByDoctorAndStatus(doctorId, status, pageable, Long.parseLong(auth.getName()), SecurityUtils.extractRole(auth)));
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

    @PatchMapping("/{id}/priority")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPTIONIST')")
    @Operation(summary = "PATCH /api/appointments/{id}/priority — set an appointment's priority (body: {priority})")
    public ResponseEntity<AppointmentResponse> setPriority(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentPriorityRequest body,
            Authentication auth) {
        return ResponseEntity.ok(appointmentService.setPriority(id, body.getPriority(), Long.parseLong(auth.getName()), SecurityUtils.extractRole(auth)));
    }

    @GetMapping("/personal/{doctorId}/board")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPTIONIST')")
    @Operation(summary = "GET /api/appointments/personal/{doctorId}/board — appointments for a personal grouped by status, filtered by ?from={date}&to={date}")
    public ResponseEntity<Map<AppointmentStatus, List<AppointmentBoardItem>>> getDoctorBoardByRange(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication auth) {
        return ResponseEntity.ok(appointmentService.getDoctorBoardByRange(doctorId, from, to, Long.parseLong(auth.getName()), SecurityUtils.extractRole(auth)));
    }

    @GetMapping("/client/{clientId}/board")
    @Operation(summary = "GET /api/appointments/client/{clientId}/board — appointments for a patient grouped by status, filtered by ?from={date}&to={date}")
    public ResponseEntity<Map<AppointmentStatus, List<AppointmentBoardItem>>> getClientBoardByRange(
            @PathVariable Long clientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication auth) {
        return ResponseEntity.ok(appointmentService.getClientBoardByRange(clientId, from, to, Long.parseLong(auth.getName()), SecurityUtils.extractRole(auth)));
    }

    @GetMapping("/personal/{doctorId}/calendar")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPTIONIST')")
    @Operation(summary = "GET /api/appointments/personal/{doctorId}/calendar — appointments for a personal grouped by day, filtered by ?month={month}&year={year}")
    public ResponseEntity<Map<String, List<AppointmentCalendarItem>>> getDoctorCalendar(
            @PathVariable Long doctorId,
            @RequestParam int month,
            @RequestParam int year,
            Authentication auth) {
        return ResponseEntity.ok(appointmentService.getDoctorCalendar(doctorId, month, year, Long.parseLong(auth.getName()), SecurityUtils.extractRole(auth)));
    }

    @GetMapping("/client/{clientId}/calendar")
    @Operation(summary = "GET /api/appointments/client/{clientId}/calendar — appointments for a patient grouped by day, filtered by ?month={month}&year={year}")
    public ResponseEntity<Map<String, List<AppointmentCalendarItem>>> getClientCalendar(
            @PathVariable Long clientId,
            @RequestParam int month,
            @RequestParam int year,
            Authentication auth) {
        return ResponseEntity.ok(appointmentService.getClientCalendar(clientId, month, year, Long.parseLong(auth.getName()), SecurityUtils.extractRole(auth)));
    }
}
