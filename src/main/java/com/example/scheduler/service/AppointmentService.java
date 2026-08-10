package com.example.scheduler.service;

import com.example.scheduler.dto.appintment.AppointmentBoardItem;
import com.example.scheduler.dto.appintment.AppointmentCalendarItem;
import com.example.scheduler.dto.appintment.AppointmentRequest;
import com.example.scheduler.dto.appintment.AppointmentResponse;
import com.example.scheduler.enums.AppointmentPriority;
import com.example.scheduler.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AppointmentService {
    AppointmentResponse book(AppointmentRequest request, Long userId, String role);
    AppointmentResponse findById(Long id);
    Page<AppointmentResponse> findByClientId(Long clientId, Pageable pageable, Long userId, String role);
    Page<AppointmentResponse> findByDoctorAndStatus(Long doctorId, AppointmentStatus status, Pageable pageable, Long userId, String role);
    AppointmentResponse confirm(Long id, Long userId, String role);
    AppointmentResponse cancel(Long id);
    AppointmentResponse reschedule(Long id, Long newScheduleId);
    AppointmentResponse setPriority(Long id, AppointmentPriority priority, Long userId, String role);
    Map<AppointmentStatus, List<AppointmentBoardItem>> getDoctorBoardByRange(Long doctorId, LocalDate from, LocalDate to, Long userId, String role);
    Map<AppointmentStatus, List<AppointmentBoardItem>> getClientBoardByRange(Long clientId, LocalDate from, LocalDate to, Long userId, String role);
    Map<String, List<AppointmentCalendarItem>> getDoctorCalendar(Long doctorId, int month, int year, Long userId, String role);
    Map<String, List<AppointmentCalendarItem>> getClientCalendar(Long clientId, int month, int year, Long userId, String role);
}
