package com.example.scheduler.service;

import com.example.scheduler.dto.appointment.AppointmentRequest;
import com.example.scheduler.dto.appointment.AppointmentResponse;
import com.example.scheduler.dto.appointment.AppointmentSummaryItem;
import com.example.scheduler.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AppointmentService {
    AppointmentResponse book(AppointmentRequest request, Long userId, String role);
    AppointmentResponse findById(Long id);
    Page<AppointmentResponse> findAppointments(Long doctorId, Long clientId, AppointmentStatus status, Pageable pageable, Long userId, String role);
    AppointmentResponse confirm(Long id, Long userId, String role);
    AppointmentResponse cancel(Long id);
    AppointmentResponse reschedule(Long id, Long newScheduleId);
    Map<AppointmentStatus, List<AppointmentSummaryItem>> getBoardByRange(LocalDate from, LocalDate to, Long doctorId, Long clientId, Long userId, String role);
    Map<String, List<AppointmentSummaryItem>> getCalendar(int month, int year, Long doctorId, Long clientId, Long userId, String role);
}
