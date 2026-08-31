package com.example.scheduler.controller;

import com.example.scheduler.dto.appointment.AppointmentRequest;
import com.example.scheduler.dto.appointment.AppointmentResponse;
import com.example.scheduler.dto.appointment.AppointmentSummaryItem;
import com.example.scheduler.dto.schedule.RescheduleRequest;
import com.example.scheduler.enums.AppointmentStatus;
import com.example.scheduler.enums.ERole;
import com.example.scheduler.security.SecurityUtils;
import com.example.scheduler.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "Appointments", description = "Book, confirmAppointmentById, cancelAppointmentById, and rescheduleAppointmentById appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;

    @PostMapping("/patient")
    @Operation(summary = "POST /api/appointments — book an appointment for a patient on a given schedule slot")
    public ResponseEntity<AppointmentResponse> bookAppointment(@Valid @RequestBody AppointmentRequest request, Authentication auth) {
        if(SecurityUtils.extractRole(auth).equals(ERole.PATIENT.name())){
            return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.bookAppointment(request, Long.parseLong(auth.getName())));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.bookAppointment(request));
    }

    @GetMapping("/{appointmentId}")
    @Operation(summary = "GET /api/appointments/{id} — get appointment details by ID")
    public ResponseEntity<AppointmentResponse> findAppointmentById(@PathVariable Long appointmentId, Authentication auth) {
        return ResponseEntity.ok(appointmentService.findAppointmentById(appointmentId, Long.parseLong(auth.getName()), SecurityUtils.extractRole(auth)));
    }

    @GetMapping
    @Operation(summary = "GET /api/appointments — list appointments, filtered by ?doctorId={id}&patientId={id}&status={status} (all optional; DOCTOR/PATIENT callers are scoped to themselves, RECEPTIONIST can filter freely or omit both for a clinic-wide list)")
    public ResponseEntity<Page<AppointmentResponse>> findAllAppointments(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) AppointmentStatus status,
            @PageableDefault(sort = "schedule.startTime", direction = Sort.Direction.ASC) Pageable pageable,
            Authentication auth
    ) {
        String role = SecurityUtils.extractRole(auth);
        if (role.equals(ERole.DOCTOR.name())){
            return ResponseEntity.ok(appointmentService.findAllAppointments(Long.parseLong(auth.getName()), patientId, status, pageable));
        }
        if (role.equals(ERole.PATIENT.name())){
            return ResponseEntity.ok(appointmentService.findAllAppointments(doctorId, Long.parseLong(auth.getName()), status, pageable));
        }
        return ResponseEntity.ok(appointmentService.findAllAppointments(doctorId, patientId, status, pageable));
    }

    @PatchMapping("/{appointmentId}/confirm")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPTIONIST')")
    @Operation(summary = "PATCH /api/appointments/{id}/confirmAppointmentById — confirmAppointmentById a pending appointment")
    public ResponseEntity<AppointmentResponse> confirmAppointmentById(@PathVariable Long appointmentId, Authentication auth) {
        return ResponseEntity.ok(appointmentService.confirmAppointmentById(appointmentId, Long.parseLong(auth.getName()), SecurityUtils.extractRole(auth)));
    }

    @PatchMapping("/{appointmentId}/cancel")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPTIONIST')")
    @Operation(summary = "PATCH /api/appointments/{id}/cancelAppointmentById — cancelAppointmentById an appointment, releases the slot back to AVAILABLE")
    public ResponseEntity<AppointmentResponse> cancelAppointmentById(@PathVariable Long appointmentId, Authentication auth) {
        return ResponseEntity.ok(appointmentService.cancelAppointmentById(appointmentId, Long.parseLong(auth.getName()), SecurityUtils.extractRole(auth)));
    }

    @PatchMapping("/{appointmentId}/reschedule")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPTIONIST')")
    @Operation(summary = "PATCH /api/appointments/{id}/rescheduleAppointmentById — move an appointment to a new schedule slot (body: {scheduleId})")
    public ResponseEntity<AppointmentResponse> rescheduleAppointmentById(@PathVariable Long appointmentId, @RequestBody RescheduleRequest request, Authentication auth) {
        return ResponseEntity.ok(appointmentService.rescheduleAppointmentById(appointmentId, request, Long.parseLong(auth.getName()), SecurityUtils.extractRole(auth)));
    }

    @GetMapping("/board")
    @Operation(summary = "GET /api/appointments/board — appointments grouped by status, filtered by ?from={date}&to={date}&doctorId={id}&clientId={id} (doctorId/clientId optional; DOCTOR/PATIENT callers are scoped to themselves, RECEPTIONIST can filter freely or omit both for a clinic-wide view)")
    public ResponseEntity<Map<AppointmentStatus, List<AppointmentSummaryItem>>> getBoardByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long patientId,
            Authentication auth) {
        String role = SecurityUtils.extractRole(auth);
        if (role.equals(ERole.DOCTOR.name())){
            return ResponseEntity.ok(appointmentService.getBoardByRange(from, to, Long.parseLong(auth.getName()), patientId));
        }
        if (role.equals(ERole.PATIENT.name())){
            return ResponseEntity.ok(appointmentService.getBoardByRange(from, to, doctorId, Long.parseLong(auth.getName())));
        }
        return ResponseEntity.ok(appointmentService.getBoardByRange(from, to, doctorId, patientId));
    }

    @GetMapping("/calendar")
    @Operation(summary = "GET /api/appointments/calendar — appointments grouped by day, filtered by ?month={month}&year={year}&doctorId={id}&clientId={id} (doctorId/clientId optional; DOCTOR/PATIENT callers are scoped to themselves, RECEPTIONIST can filter freely or omit both for a clinic-wide view)")
    public ResponseEntity<Map<String, List<AppointmentSummaryItem>>> getCalendar(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long patientId,
            Authentication auth) {
        String role = SecurityUtils.extractRole(auth);
        if (role.equals(ERole.DOCTOR.name())){
            return ResponseEntity.ok(appointmentService.getCalendar(month, year, Long.parseLong(auth.getName()), patientId));
        }
        if (role.equals(ERole.PATIENT.name())){
            return ResponseEntity.ok(appointmentService.getCalendar(month, year, doctorId, Long.parseLong(auth.getName())));
        }
        return ResponseEntity.ok(appointmentService.getCalendar(month, year, doctorId, patientId));
    }
}
