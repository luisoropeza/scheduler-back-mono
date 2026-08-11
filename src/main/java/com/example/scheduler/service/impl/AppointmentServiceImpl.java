package com.example.scheduler.service.impl;

import com.example.scheduler.dto.appointment.AppointmentRequest;
import com.example.scheduler.dto.appointment.AppointmentResponse;
import com.example.scheduler.dto.appointment.AppointmentSummaryItem;
import com.example.scheduler.entity.Appointment;
import com.example.scheduler.entity.Patient;
import com.example.scheduler.entity.Personal;
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
import com.example.scheduler.repository.PersonalRepository;
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
    private final PersonalRepository personalRepository;
    private final AppointmentMapper appointmentMapper;

    @Override
    @Transactional
    public AppointmentResponse book(AppointmentRequest request, Long userId, String role) {
        Schedule schedule = getScheduleOrThrow(request.getScheduleId());
        if (!ScheduleStatus.AVAILABLE.equals(schedule.getStatus()))
            throw new BusinessException("This schedule slot is no longer available");
        if (schedule.getStartTime().isBefore(LocalDateTime.now()))
            throw new BusinessException("Cannot book a past schedule slot");
        Patient patient = getPatientOrThrow(request.getClientId());
        if (role.equals(ERole.PATIENT.name()) && !patient.getAccount().getId().equals(userId))
            throw new ForbiddenException("A patient can only book appointments for themselves");
        schedule.setStatus(ScheduleStatus.BOOKED);
        Appointment appointment = Appointment.builder()
                .schedule(schedule)
                .patient(patient)
                .build();
        if (!role.equals(ERole.PATIENT.name())) {
            appointment.setStatus(AppointmentStatus.CONFIRMED);
        }
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    public AppointmentResponse findById(Long id) {
        return appointmentMapper.toResponse(getOrThrow(id));
    }

    @Override
    public Page<AppointmentResponse> findAppointments(Long doctorId, Long clientId, AppointmentStatus status, Pageable pageable, Long userId, String role) {
        Long resolvedDoctorId = resolveDoctorFilter(doctorId, userId, role);
        Long resolvedClientId = resolveClientFilter(clientId, userId, role);
        return appointmentRepository.findAllByFilters(resolvedDoctorId, resolvedClientId, status, pageable).map(appointmentMapper::toResponse);
    }

    @Override
    @Transactional
    public AppointmentResponse confirm(Long id, Long userId, String role) {
        Appointment appointment = getOrThrow(id);
        if(role.equals(ERole.DOCTOR.name()))
            if (!appointment.getSchedule().getDoctor().getAccount().getId().equals(userId))
                throw new ForbiddenException("Not authorized to confirm this appointment");
        if (appointment.getStatus() != AppointmentStatus.PENDING)
            throw new BusinessException("Only pending appointments can be confirmed");
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentResponse cancel(Long id) {
        Appointment appointment = getOrThrow(id);
        if (appointment.getStatus() == AppointmentStatus.CANCELLED)
            throw new BusinessException("Appointment is already cancelled");
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.getSchedule().setStatus(ScheduleStatus.AVAILABLE);
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentResponse reschedule(Long id, Long newScheduleId) {
        Appointment appointment = getOrThrow(id);
        if (appointment.getStatus() == AppointmentStatus.CANCELLED)
            throw new BusinessException("Cannot reschedule a cancelled appointment");
        Schedule newSchedule = getScheduleOrThrow(newScheduleId);
        if (newSchedule.getStatus() != ScheduleStatus.AVAILABLE)
            throw new BusinessException("New schedule slot is not available");
        if (newSchedule.getStartTime().isBefore(LocalDateTime.now()))
            throw new BusinessException("Cannot reschedule to a past slot");
        newSchedule.setStatus(ScheduleStatus.BOOKED);
        appointment.getSchedule().setStatus(ScheduleStatus.AVAILABLE);
        appointment.setSchedule(newSchedule);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    public Map<AppointmentStatus, List<AppointmentSummaryItem>> getBoardByRange(LocalDate from, LocalDate to, Long doctorId, Long clientId, Long userId, String role) {
        Long resolvedDoctorId = resolveDoctorFilter(doctorId, userId, role);
        Long resolvedClientId = resolveClientFilter(clientId, userId, role);
        return groupByStatus(appointmentRepository.findByFiltersAndDateRange(resolvedDoctorId, resolvedClientId, from.atStartOfDay(), to.plusDays(1).atStartOfDay()));
    }

    @Override
    public Map<String, List<AppointmentSummaryItem>> getCalendar(int month, int year, Long doctorId, Long clientId, Long userId, String role) {
        Long resolvedDoctorId = resolveDoctorFilter(doctorId, userId, role);
        Long resolvedClientId = resolveClientFilter(clientId, userId, role);
        LocalDate monthStart = LocalDate.of(year, month, 1);
        return groupByDay(appointmentRepository.findByFiltersAndDateRange(resolvedDoctorId, resolvedClientId, monthStart.atStartOfDay(), monthStart.plusMonths(1).atStartOfDay()));
    }

    private Long resolveDoctorFilter(Long doctorId, Long userId, String role) {
        if (!role.equals(ERole.DOCTOR.name()))
            return doctorId;
        Long callerDoctorId = resolveCallerDoctorId(userId);
        if (doctorId != null && !doctorId.equals(callerDoctorId))
            throw new ForbiddenException("Not authorized to get those appointments");
        return callerDoctorId;
    }

    private Long resolveClientFilter(Long clientId, Long userId, String role) {
        if (!role.equals(ERole.PATIENT.name()))
            return clientId;
        Long callerClientId = resolveCallerPatientId(userId);
        if (clientId != null && !clientId.equals(callerClientId))
            throw new ForbiddenException("Not authorized to get those appointments");
        return callerClientId;
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
            calendar.computeIfAbsent(key, k -> new ArrayList<>()).add(toSummaryItem(appointment));
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

    private Appointment getOrThrow(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
    }

    private Schedule getScheduleOrThrow(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));
    }

    private Patient getPatientOrThrow(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
    }

    private Long resolveCallerPatientId(Long accountId) {
        return patientRepository.findByAccount_Id(accountId)
                .map(Patient::getId)
                .orElse(null);
    }

    private Long resolveCallerDoctorId(Long accountId) {
        return personalRepository.findByAccount_Id(accountId)
                .map(Personal::getId)
                .orElse(null);
    }
}
