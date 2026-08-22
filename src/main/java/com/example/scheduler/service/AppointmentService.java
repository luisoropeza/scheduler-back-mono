package com.example.scheduler.service;

import com.example.scheduler.dto.appointment.AppointmentRequest;
import com.example.scheduler.dto.appointment.AppointmentResponse;
import com.example.scheduler.dto.appointment.AppointmentSummaryItem;
import com.example.scheduler.dto.schedule.RescheduleRequest;
import com.example.scheduler.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AppointmentService {
    AppointmentResponse bookAppointment(AppointmentRequest request, Long userId, String role);
    AppointmentResponse findAppointmentById(Long id);
    AppointmentResponse findAppointmentByIdAndDoctorId(Long doctorId, Long id);
    AppointmentResponse findAppointmentByIdAndPatientId(Long patientId, Long id);
    Page<AppointmentResponse> findAllAppointments(Long doctorId, Long patientId, AppointmentStatus status, Pageable pageable);
    AppointmentResponse confirmAppointmentById(Long id, Long patientId, String role);
    AppointmentResponse cancelAppointmentById(Long id);
    AppointmentResponse rescheduleAppointmentById(Long id, RescheduleRequest request);
    Map<AppointmentStatus, List<AppointmentSummaryItem>> getBoardByRange(LocalDate from, LocalDate to, Long doctorId, Long patientId, Long userId, String role);
    Map<String, List<AppointmentSummaryItem>> getCalendar(int month, int year, Long doctorId, Long patientId, Long userId, String role);
}
