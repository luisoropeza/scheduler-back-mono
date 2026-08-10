package com.example.scheduler.service.impl;

import com.example.scheduler.dto.appintment.AppointmentBoardItem;
import com.example.scheduler.dto.appintment.AppointmentCalendarItem;
import com.example.scheduler.dto.appintment.AppointmentRequest;
import com.example.scheduler.dto.appintment.AppointmentResponse;
import com.example.scheduler.entity.Appointment;
import com.example.scheduler.entity.Patient;
import com.example.scheduler.entity.Schedule;
import com.example.scheduler.enums.AppointmentPriority;
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
    public AppointmentResponse book(AppointmentRequest request, Long userId, String role) {
        if (role.equals(ERole.PATIENT.name()) && !request.getClientId().equals(userId))
            throw new ForbiddenException("A patient can only book appointments for themselves");
        Schedule schedule = getScheduleOrThrow(request.getScheduleId());
        if (!ScheduleStatus.AVAILABLE.equals(schedule.getStatus()))
            throw new BusinessException("This schedule slot is no longer available");
        if (schedule.getStartTime().isBefore(LocalDateTime.now()))
            throw new BusinessException("Cannot book a past schedule slot");
        Patient patient = getPatientOrThrow(request.getClientId());
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
    public Page<AppointmentResponse> findByClientId(Long clientId, Pageable pageable, Long userId, String role) {
        if (role.equals(ERole.PATIENT.name()))
            if (!clientId.equals(userId))
                throw new ForbiddenException("Not authorized to get those appointments");
        return appointmentRepository.findByPatientId(clientId, pageable).map(appointmentMapper::toResponse);
    }

    @Override
    public Page<AppointmentResponse> findByDoctorAndStatus(Long doctorId, AppointmentStatus status, Pageable pageable, Long userId, String role) {
        if(role.equals(ERole.DOCTOR.name()))
            if (!doctorId.equals(userId))
                throw new ForbiddenException("Not authorized to get those appointments");
        return appointmentRepository.findAllByFilters(doctorId, status, pageable).map(appointmentMapper::toResponse);
    }

    @Override
    @Transactional
    public AppointmentResponse confirm(Long id, Long userId, String role) {
        Appointment appointment = getOrThrow(id);
        if(role.equals(ERole.DOCTOR.name()))
            if (!appointment.getSchedule().getDoctor().getId().equals(userId))
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
    @Transactional
    public AppointmentResponse setPriority(Long id, AppointmentPriority priority, Long userId, String role) {
        Appointment appointment = getOrThrow(id);
        if (role.equals(ERole.DOCTOR.name()))
            if (!appointment.getSchedule().getDoctor().getId().equals(userId))
                throw new ForbiddenException("Not authorized to update this appointment's priority");
        appointment.setPriority(priority);
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    public Map<AppointmentStatus, List<AppointmentBoardItem>> getDoctorBoardByRange(Long doctorId, LocalDate from, LocalDate to, Long userId, String role) {
        if (role.equals(ERole.DOCTOR.name()))
            if (!doctorId.equals(userId))
                throw new ForbiddenException("Not authorized to get those appointments");
        return groupByStatus(appointmentRepository.findByDoctorAndDateRange(doctorId, from.atStartOfDay(), to.plusDays(1).atStartOfDay()));
    }

    @Override
    public Map<AppointmentStatus, List<AppointmentBoardItem>> getClientBoardByRange(Long clientId, LocalDate from, LocalDate to, Long userId, String role) {
        if (role.equals(ERole.PATIENT.name()))
            if (!clientId.equals(userId))
                throw new ForbiddenException("Not authorized to get those appointments");
        return groupByStatus(appointmentRepository.findByPatientAndDateRange(clientId, from.atStartOfDay(), to.plusDays(1).atStartOfDay()));
    }

    @Override
    public Map<String, List<AppointmentCalendarItem>> getDoctorCalendar(Long doctorId, int month, int year, Long userId, String role) {
        if (role.equals(ERole.DOCTOR.name()))
            if (!doctorId.equals(userId))
                throw new ForbiddenException("Not authorized to get those appointments");
        LocalDate monthStart = LocalDate.of(year, month, 1);
        return groupByDay(appointmentRepository.findByDoctorAndDateRange(doctorId, monthStart.atStartOfDay(), monthStart.plusMonths(1).atStartOfDay()));
    }

    @Override
    public Map<String, List<AppointmentCalendarItem>> getClientCalendar(Long clientId, int month, int year, Long userId, String role) {
        if (role.equals(ERole.PATIENT.name()))
            if (!clientId.equals(userId))
                throw new ForbiddenException("Not authorized to get those appointments");
        LocalDate monthStart = LocalDate.of(year, month, 1);
        return groupByDay(appointmentRepository.findByPatientAndDateRange(clientId, monthStart.atStartOfDay(), monthStart.plusMonths(1).atStartOfDay()));
    }

    private Map<AppointmentStatus, List<AppointmentBoardItem>> groupByStatus(List<Appointment> appointments) {
        Map<AppointmentStatus, List<AppointmentBoardItem>> board = new EnumMap<>(AppointmentStatus.class);
        for (AppointmentStatus status : AppointmentStatus.values())
            board.put(status, new ArrayList<>());
        for (Appointment appointment : appointments)
            board.get(appointment.getStatus()).add(toBoardItem(appointment));
        return board;
    }

    private Map<String, List<AppointmentCalendarItem>> groupByDay(List<Appointment> appointments) {
        Map<String, List<AppointmentCalendarItem>> calendar = new LinkedHashMap<>();
        for (Appointment appointment : appointments) {
            String key = appointment.getSchedule().getStartTime().toLocalDate().format(CALENDAR_KEY_FORMATTER);
            calendar.computeIfAbsent(key, k -> new ArrayList<>()).add(toCalendarItem(appointment));
        }
        return calendar;
    }

    private AppointmentBoardItem toBoardItem(Appointment appointment) {
        LocalDateTime startTime = appointment.getSchedule().getStartTime();
        return AppointmentBoardItem.builder()
                .clientName(appointment.getPatient().getAccount().getName())
                .doctorName(appointment.getSchedule().getDoctor().getAccount().getName())
                .appointmentDate(startTime.toLocalDate())
                .appointmentTime(startTime.format(TIME_FORMATTER))
                .priority(appointment.getPriority().getDisplayName())
                .build();
    }

    private AppointmentCalendarItem toCalendarItem(Appointment appointment) {
        return AppointmentCalendarItem.builder()
                .clientName(appointment.getPatient().getAccount().getName())
                .doctorName(appointment.getSchedule().getDoctor().getAccount().getName())
                .appointmentTime(appointment.getSchedule().getStartTime().format(TIME_FORMATTER))
                .priority(appointment.getPriority().getDisplayName())
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
}
