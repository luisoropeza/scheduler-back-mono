package com.example.scheduler.controller;

import com.example.scheduler.dto.appointment.AppointmentPersonalRequest;
import com.example.scheduler.dto.appointment.AppointmentResponse;
import com.example.scheduler.dto.appointment.AppointmentSummaryItem;
import com.example.scheduler.dto.patient.PatientResponse;
import com.example.scheduler.dto.personal.PersonalResponse;
import com.example.scheduler.dto.schedule.RescheduleRequest;
import com.example.scheduler.entity.Patient;
import com.example.scheduler.entity.Personal;
import com.example.scheduler.enums.AppointmentStatus;
import com.example.scheduler.repository.PatientRepository;
import com.example.scheduler.repository.PersonalRepository;
import com.example.scheduler.security.SecurityUtils;
import com.example.scheduler.service.AppointmentService;
import com.example.scheduler.service.PatientService;
import com.example.scheduler.service.PersonalService;
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
    private final PersonalService personalService;
    private final PatientService patientService;

    @PostMapping
    @Operation(summary = "POST /api/appointments — book an appointment for a patient on a given schedule slot")
    public ResponseEntity<AppointmentResponse> bookAppointment(@Valid @RequestBody AppointmentPersonalRequest request, Authentication auth) {
        PatientResponse patient = patientService.findBySelf(Long.parseLong(auth.getName()));
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.bookAppointment(request, patient.getId(), SecurityUtils.extractRole(auth)));
    }
    @GetMapping("/{id}")
    @Operation(summary = "GET /api/appointments/{id} — get appointment details by ID")
    public ResponseEntity<AppointmentResponse> findAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.findAppointmentById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RECEPCIONIST')")
    @Operation(summary = "GET /api/appointments — list appointments, filtered by ?doctorId={id}&patientId={id}&status={status} (all optional; DOCTOR/PATIENT callers are scoped to themselves, RECEPTIONIST can filter freely or omit both for a clinic-wide list)")
    public ResponseEntity<Page<AppointmentResponse>> findAllAppointments(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) AppointmentStatus status,
            @PageableDefault(sort = "schedule.startTime", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(appointmentService.findAllAppointments(doctorId, patientId, status, pageable));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR')")
    @Operation(summary = "GET /api/appointments — list appointments, filtered by ?doctorId={id}&patientId={id}&status={status} (all optional; DOCTOR/PATIENT callers are scoped to themselves, RECEPTIONIST can filter freely or omit both for a clinic-wide list)")
    public ResponseEntity<Page<AppointmentResponse>> findAllAppointmentsByDoctor(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) AppointmentStatus status,
            @PageableDefault(sort = "schedule.startTime", direction = Sort.Direction.ASC) Pageable pageable,
            Authentication auth
    ) {
        PersonalResponse personal = personalService.findBySelf(Long.parseLong(auth.getName()));
        return ResponseEntity.ok(appointmentService.findAllAppointments(personal.getId(), patientId, status, pageable));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PATIENT')")
    @Operation(summary = "GET /api/appointments — list appointments, filtered by ?doctorId={id}&patientId={id}&status={status} (all optional; DOCTOR/PATIENT callers are scoped to themselves, RECEPTIONIST can filter freely or omit both for a clinic-wide list)")
    public ResponseEntity<Page<AppointmentResponse>> findAllAppointmentsByPatient(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) AppointmentStatus status,
            @PageableDefault(sort = "schedule.startTime", direction = Sort.Direction.ASC) Pageable pageable,
            Authentication auth
    ) {
        PatientResponse patient = patientService.findBySelf(Long.parseLong(auth.getName()));
        return ResponseEntity.ok(appointmentService.findAllAppointments(doctorId, patient.getId(), status, pageable));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPTIONIST')")
    @Operation(summary = "PATCH /api/appointments/{id}/confirmAppointmentById — confirmAppointmentById a pending appointment")
    public ResponseEntity<AppointmentResponse> confirmAppointmentById(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(appointmentService.confirmAppointmentById(id, Long.parseLong(auth.getName()), SecurityUtils.extractRole(auth)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPTIONIST')")
    @Operation(summary = "PATCH /api/appointments/{id}/cancelAppointmentById — cancelAppointmentById an appointment, releases the slot back to AVAILABLE")
    public ResponseEntity<AppointmentResponse> cancelAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.cancelAppointmentById(id));
    }

    @PatchMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPTIONIST')")
    @Operation(summary = "PATCH /api/appointments/{id}/rescheduleAppointmentById — move an appointment to a new schedule slot (body: {scheduleId})")
    public ResponseEntity<AppointmentResponse> rescheduleAppointmentById(
            @PathVariable Long id,
            @RequestBody RescheduleRequest request) {
        return ResponseEntity.ok(appointmentService.rescheduleAppointmentById(id, request));
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
