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
    AppointmentResponse bookAppointment(AppointmentRequest request, Long patientId);
    AppointmentResponse bookAppointment(AppointmentRequest request);
    AppointmentResponse findAppointmentById(Long appointmentId, Long accountId, String role);
    Page<AppointmentResponse> findAllAppointments(Long doctorId, Long patientId, AppointmentStatus status, Pageable pageable);
    AppointmentResponse confirmAppointmentById(Long AppointmentId, Long accountId, String role);
    AppointmentResponse cancelAppointmentById(Long AppointmentId, Long accountId, String role);
    AppointmentResponse rescheduleAppointmentById(Long AppointmentId, RescheduleRequest request, Long accountId, String role);
    Map<AppointmentStatus, List<AppointmentSummaryItem>> getBoardByRange(LocalDate from, LocalDate to, Long doctorId, Long patientId, Long userId, String role);
    Map<String, List<AppointmentSummaryItem>> getCalendar(int month, int year, Long doctorId, Long patientId, Long userId, String role);
}
