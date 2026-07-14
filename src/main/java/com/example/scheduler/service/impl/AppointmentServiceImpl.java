package com.example.scheduler.service.impl;

import com.example.scheduler.dto.AppointmentRequest;
import com.example.scheduler.dto.AppointmentResponse;
import com.example.scheduler.entity.Appointment;
import com.example.scheduler.entity.Patient;
import com.example.scheduler.entity.Schedule;
import com.example.scheduler.enums.AppointmentStatus;
import com.example.scheduler.enums.ERole;
import com.example.scheduler.enums.ScheduleStatus;
import com.example.scheduler.exception.BusinessException;
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

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentServiceImpl implements AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final PatientRepository patientRepository;
    private final AppointmentMapper appointmentMapper;

    @Override
    @Transactional
    public AppointmentResponse book(AppointmentRequest request, Long userId, String role) {
        if (role.equals(ERole.PATIENT.name()) && !request.getClientId().equals(userId))
            throw new BusinessException("A patient can only book appointments for themselves");
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
                throw new BusinessException("Not authorized to get those appointments");
        return appointmentRepository.findByPatientId(clientId, pageable).map(appointmentMapper::toResponse);
    }

    @Override
    public Page<AppointmentResponse> findByDoctorAndStatus(Long doctorId, AppointmentStatus status, Pageable pageable, Long userId, String role) {
        if(role.equals(ERole.DOCTOR.name()))
            if (!doctorId.equals(userId))
                throw new BusinessException("Not authorized to get those appointments");
        return appointmentRepository.findAllByFilters(doctorId, status, pageable).map(appointmentMapper::toResponse);
    }

    @Override
    @Transactional
    public AppointmentResponse confirm(Long id, Long userId, String role) {
        Appointment appointment = getOrThrow(id);
        if(role.equals(ERole.DOCTOR.name()))
            if (!appointment.getSchedule().getDoctor().getId().equals(userId))
                throw new BusinessException("Not authorized to confirm this appointment");
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
