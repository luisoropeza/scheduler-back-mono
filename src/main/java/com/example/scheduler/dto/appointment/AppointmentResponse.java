package com.example.scheduler.dto.appointment;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentResponse {
    private Long id;

    // Schedule
    private Long scheduleId;
    private LocalDateTime scheduleStart;
    private LocalDateTime scheduleEnd;

    // Doctor
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialty;
    private String doctorEmail;

    // Client
    private Long clientId;
    private String clientName;
    private String clientEmail;

    private String status;
    private LocalDateTime createdAt;
}
