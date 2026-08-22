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
    public AppointmentResponse bookAppointment(AppointmentRequest request, Long userId, String role) {
        boolean isPatient = role.equals(ERole.PATIENT.name());
        if(isPatient && userId.equals(request.getPatientId()))
            throw new ForbiddenException("Este usuario no puede crear una cita en este recurso");
        Schedule schedule = getScheduleOrThrow(request.getScheduleId());
        Patient patient = getPatientOrThrow(request.getPatientId());
        validateAvailabilityAndDate(schedule);
        schedule.setStatus(ScheduleStatus.BOOKED);
        Appointment appointment = Appointment.builder()
                .schedule(schedule)
                .patient(patient)
                .status(isPatient? AppointmentStatus.PENDING : AppointmentStatus.CONFIRMED)
                .build();
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    public AppointmentResponse findAppointmentById(Long id) {
        return appointmentMapper.toResponse(getAppointmentOrThrow(id));
    }

    @Override
    public AppointmentResponse findAppointmentByIdAndDoctorId(Long doctorId, Long id) {
        return appointmentMapper.toResponse(getAppointmentOrThrowByDoctor(id, doctorId));
    }

    @Override
    public AppointmentResponse findAppointmentByIdAndPatientId(Long patientId, Long id) {
        return appointmentMapper.toResponse(getAppointmentOrThrowByPatient(id, patientId));
    }

    @Override
    public Page<AppointmentResponse> findAllAppointments(Long doctorId, Long patientId, AppointmentStatus status, Pageable pageable) {
        return appointmentRepository.findAllByFilters(doctorId, patientId, status, pageable).map(appointmentMapper::toResponse);
    }

    @Override
    @Transactional
    public AppointmentResponse confirmAppointmentById(Long id, Long patientId, String role) {
        Appointment appointment = getAppointmentOrThrow(id);
        if(role.equals(ERole.DOCTOR.name()))
            if (!appointment.getSchedule().getDoctor().getAccount().getId().equals(patientId))
                throw new ForbiddenException("Not authorized to confirmAppointmentById this appointment");
        if (appointment.getStatus() != AppointmentStatus.PENDING)
            throw new BusinessException("Only pending appointments can be confirmed");
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentResponse cancelAppointmentById(Long id) {
        Appointment appointment = getAppointmentOrThrow(id);
        if (appointment.getStatus() == AppointmentStatus.CANCELLED)
            throw new BusinessException("Appointment is already cancelled");
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.getSchedule().setStatus(ScheduleStatus.AVAILABLE);
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentResponse rescheduleAppointmentById(Long id, RescheduleRequest request) {
        Appointment appointment = getAppointmentOrThrow(id);
        Schedule newSchedule = getScheduleOrThrow(request.getScheduleId());
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

    private Appointment getAppointmentOrThrow(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
    }

    private Appointment getAppointmentOrThrowByDoctor(Long id, Long doctorId) {
        return appointmentRepository.findByIdAndScheduleDoctorId(id, doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
    }

    private Appointment getAppointmentOrThrowByPatient(Long id, Long patientId) {
        return appointmentRepository.findByIdAndPatientId(id, patientId)
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

    private void validateAvailabilityAndDate(Schedule schedule) {
        if (!ScheduleStatus.AVAILABLE.equals(schedule.getStatus()))
            throw new BusinessException("This schedule slot is no longer available");
        if (schedule.getStartTime().isBefore(LocalDateTime.now()))
            throw new BusinessException("Cannot bookAppointment a past schedule slot");
    }
}
