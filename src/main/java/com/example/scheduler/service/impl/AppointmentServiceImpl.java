package com.example.scheduler.service.impl;

import com.example.scheduler.dto.appointment.AppointmentRequest;
import com.example.scheduler.dto.appointment.AppointmentResponse;
import com.example.scheduler.dto.appointment.AppointmentSummaryItem;
import com.example.scheduler.dto.schedule.RescheduleRequest;
import com.example.scheduler.entity.Appointment;
import com.example.scheduler.entity.Patient;
import com.example.scheduler.entity.Schedule;
import com.example.scheduler.enums.AppointmentStatus;
import com.example.scheduler.enums.ERole;
import com.example.scheduler.enums.ScheduleStatus;
import com.example.scheduler.exception.BusinessException;
import com.example.scheduler.exception.ForbiddenException;
import com.example.scheduler.exception.ResourceNotFoundException;
import com.example.scheduler.mapper.AppointmentMapper;
import com.example.scheduler.repository.AppointmentRepository;
import com.example.scheduler.repository.PatientRepository;
import com.example.scheduler.repository.ScheduleRepository;
import com.example.scheduler.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentServiceImpl implements AppointmentService {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter CALENDAR_KEY_FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    private final AppointmentRepository appointmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final PatientRepository patientRepository;
    private final AppointmentMapper appointmentMapper;

    @Override
    @Transactional
    public AppointmentResponse bookAppointment(AppointmentRequest request,  Long patientId) {
        Schedule schedule = getScheduleOrThrowById(request.getScheduleId());
        Patient patient = getPatientOrThrowById(request.getPatientId());
        if(!patientId.equals(patient.getId())){
            throw new ForbiddenException("Can't book an appointment for another patient");
        }
        validateAvailabilityAndDate(schedule);
        schedule.setStatus(ScheduleStatus.BOOKED);
        Appointment appointment = Appointment.builder()
                .schedule(schedule)
                .patient(patient)
                .status(AppointmentStatus.PENDING)
                .build();
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentResponse bookAppointment(AppointmentRequest request) {
        Schedule schedule = getScheduleOrThrowById(request.getScheduleId());
        Patient patient = getPatientOrThrowById(request.getPatientId());
        validateAvailabilityAndDate(schedule);
        schedule.setStatus(ScheduleStatus.BOOKED);
        Appointment appointment = Appointment.builder()
                .schedule(schedule)
                .patient(patient)
                .status(AppointmentStatus.CONFIRMED)
                .build();
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    public AppointmentResponse findAppointmentById(Long appointmentId, Long accountId, String role) {
        Appointment appointment = getAppointmentOrThrowById(appointmentId);
        verifyPermission(appointment, accountId, role);
        return appointmentMapper.toResponse(appointment);
    }

    @Override
    public Page<AppointmentResponse> findAllAppointments(Long doctorId, Long patientId, AppointmentStatus status, Pageable pageable) {
        return appointmentRepository.findAllByFilters(doctorId, patientId, status, pageable).map(appointmentMapper::toResponse);
    }

    @Override
    @Transactional
    public AppointmentResponse confirmAppointmentById(Long AppointmentId, Long accountId, String role) {
        Appointment appointment = getAppointmentOrThrowById(AppointmentId);
        verifyPermission(appointment, accountId, role);
        if (appointment.getStatus() != AppointmentStatus.PENDING)
            throw new BusinessException("Just can confirm an appointment pending");
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentResponse cancelAppointmentById(Long AppointmentId, Long accountId, String role) {
        Appointment appointment = getAppointmentOrThrowById(AppointmentId);
        verifyPermission(appointment, accountId, role);
        if (appointment.getStatus() == AppointmentStatus.CANCELLED)
            throw new BusinessException("This appointment is already cancelled");
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.getSchedule().setStatus(ScheduleStatus.AVAILABLE);
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentResponse rescheduleAppointmentById(Long AppointmentId, RescheduleRequest request, Long accountId, String role) {
        Appointment appointment = getAppointmentOrThrowById(AppointmentId);
        verifyPermission(appointment, accountId, role);
        Schedule newSchedule = getScheduleOrThrowById(request.getScheduleId());
        if (appointment.getStatus() == AppointmentStatus.CANCELLED)
            throw new BusinessException("Cannot rescheduleAppointmentById a cancelled appointment");
        if (newSchedule.getStatus() != ScheduleStatus.AVAILABLE)
            throw new BusinessException("New schedule slot is not available");
        if (newSchedule.getStartTime().isBefore(LocalDateTime.now()))
            throw new BusinessException("Cannot rescheduleAppointmentById to a past slot");
        newSchedule.setStatus(ScheduleStatus.BOOKED);
        appointment.getSchedule().setStatus(ScheduleStatus.AVAILABLE);
        appointment.setSchedule(newSchedule);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    public Map<AppointmentStatus, List<AppointmentSummaryItem>> getBoardByRange(LocalDate from, LocalDate to, Long doctorId, Long patientId, Long userId, String role) {
        return groupByStatus(appointmentRepository.findByFiltersAndDateRange(doctorId, patientId, from.atStartOfDay(), to.plusDays(1).atStartOfDay()));
    }

    @Override
    public Map<String, List<AppointmentSummaryItem>> getCalendar(int month, int year, Long doctorId, Long patientId, Long userId, String role) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        return groupByDay(appointmentRepository.findByFiltersAndDateRange(doctorId, patientId, monthStart.atStartOfDay(), monthStart.plusMonths(1).atStartOfDay()));
    }

    private Map<AppointmentStatus, List<AppointmentSummaryItem>> groupByStatus(List<Appointment> appointments) {
        Map<AppointmentStatus, List<AppointmentSummaryItem>> board = new EnumMap<>(AppointmentStatus.class);
        for (AppointmentStatus status : AppointmentStatus.values())
            board.put(status, new ArrayList<>());
        for (Appointment appointment : appointments)
            board.get(appointment.getStatus()).add(toSummaryItem(appointment));
        return board;
    }

    private Map<String, List<AppointmentSummaryItem>> groupByDay(List<Appointment> appointments) {
        Map<String, List<AppointmentSummaryItem>> calendar = new LinkedHashMap<>();
        for (Appointment appointment : appointments) {
            String key = appointment.getSchedule().getStartTime().toLocalDate().format(CALENDAR_KEY_FORMATTER);
            calendar.computeIfAbsent(key, _ -> new ArrayList<>()).add(toSummaryItem(appointment));
        }
        return calendar;
    }

    private AppointmentSummaryItem toSummaryItem(Appointment appointment) {
        LocalDateTime startTime = appointment.getSchedule().getStartTime();
        return AppointmentSummaryItem.builder()
                .clientName(appointment.getPatient().getAccount().getName())
                .doctorName(appointment.getSchedule().getDoctor().getAccount().getName())
                .appointmentDate(startTime.toLocalDate())
                .appointmentTime(startTime.format(TIME_FORMATTER))
                .build();
    }

    private Appointment getAppointmentOrThrowById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));
    }

    private Schedule getScheduleOrThrowById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + scheduleId));
    }

    private Patient getPatientOrThrowById(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
    }

    private void validateAvailabilityAndDate(Schedule schedule) {
        if (!ScheduleStatus.AVAILABLE.equals(schedule.getStatus()))
            throw new BusinessException("This schedule slot is no longer available");
        if (schedule.getStartTime().isBefore(LocalDateTime.now()))
            throw new BusinessException("Cannot bookAppointment a past schedule slot");
    }

    private void verifyPermission(Appointment appointment, Long accountId, String role) {
        if(role.equals(ERole.DOCTOR.name()))
            if (!appointment.getSchedule().getDoctor().getAccount().getId().equals(accountId))
                throw new ForbiddenException("Not authorize to do this");
        if(role.equals(ERole.PATIENT.name()))
            if (!appointment.getPatient().getAccount().getId().equals(accountId))
                throw new ForbiddenException("Not authorize to do this");
    }
}
