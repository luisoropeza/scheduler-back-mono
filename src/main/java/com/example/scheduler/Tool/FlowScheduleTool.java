package com.example.scheduler.Tool;

import com.example.scheduler.dto.appointment.AppointmentResponse;
import com.example.scheduler.dto.patient.PatientResponse;
import com.example.scheduler.dto.personal.PersonalResponse;
import com.example.scheduler.dto.schedule.ScheduleResponse;
import com.example.scheduler.dto.specialty.SpecialtyResponse;
import com.example.scheduler.entity.Appointment;
import com.example.scheduler.entity.Patient;
import com.example.scheduler.entity.Schedule;
import com.example.scheduler.enums.AppointmentStatus;
import com.example.scheduler.enums.ScheduleStatus;
import com.example.scheduler.exception.BusinessException;
import com.example.scheduler.exception.ResourceNotFoundException;
import com.example.scheduler.mapper.*;
import com.example.scheduler.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class FlowScheduleTool {
    private final SpecialtyRepository specialtyRepository;
    private final SpecialtyMapper specialtyMapper;
    private final PersonalRepository personalRepository;
    private final PersonalMapper personalMapper;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;
    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;


    @Tool(description = "Paso 1.- Este metodo te ayudara a obtener la informacion del usuario que se esta comunicando contigo", name = "getPatientUser")
    public PatientResponse getPatientUser(){
        String patientId = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        Patient patient = getPatientOrThrowById(Long.parseLong(patientId));
        return patientMapper.toResponse(patient);
    }

    @Tool(description = "Paso 1.- Este metodo te devolvera las especialidades disponibles", name = "findAllSpecialties")
    public List<SpecialtyResponse> findAllSpecialties(){
        return specialtyMapper.toResponseList(specialtyRepository.findAll());
    }

    @Tool(description = "Paso 2.- Este metodo te devolvera las especialidades bajo el parametro specialtyId", name = "findAllDoctors")
    public List<PersonalResponse> findAllDoctors(Long specialtyId){
        if(specialtyId != null)
            getSpecialtyOrThrowById(specialtyId);
        return personalMapper.toResponseList(personalRepository.findAllDoctorsActive(specialtyId));
    }

    @Tool(description = "Paso 3.- Este metodo te devolvera los doctores bajo el parametro doctorId", name = "findAllSchedules")
    public List<ScheduleResponse> findAllSchedules(Long doctorId){
        if (doctorId != null)
            getActiveDoctorOrThrowById(doctorId);
        return scheduleMapper.toResponseList(
                scheduleRepository.findAllSchedulesAvailable(doctorId, LocalDateTime.now())
        );
    }

    @Transactional
    @Tool(description = "Paso 4.- Este metodo te agendara la cita mediante el scheduleId y el patientId", name = "bookAppointment")
    public AppointmentResponse bookAppointment(Long scheduleId, Long patientId){
        var schedule = getScheduleOrThrowById(scheduleId);
        var patient = getPatientOrThrowById(patientId);
        validateAvailabilityAndDate(schedule);
        schedule.setStatus(ScheduleStatus.BOOKED);
        var appointment = Appointment.builder()
                .schedule(schedule)
                .patient(patient)
                .status(AppointmentStatus.PENDING)
                .build();
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    private void getSpecialtyOrThrowById(Long specialtyId) {
        specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with id: " + specialtyId));
    }

    private void getActiveDoctorOrThrowById(Long personalId) {
        var doctor = personalRepository.findById(personalId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + personalId));
        if (!doctor.isActive()) throw new BusinessException("This doctor is not active");
    }

    private Schedule getScheduleOrThrowById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new  ResourceNotFoundException("Schedule not found"));
    }

    private Patient getPatientOrThrowById(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new  ResourceNotFoundException("Patient not found"));
    }

    private void validateAvailabilityAndDate(Schedule schedule) {
        if (!ScheduleStatus.AVAILABLE.equals(schedule.getStatus()))
            throw new BusinessException("This schedule slot is no longer available");
        if (schedule.getStartTime().isBefore(LocalDateTime.now()))
            throw new BusinessException("Cannot bookAppointment a past schedule slot");
    }
}
