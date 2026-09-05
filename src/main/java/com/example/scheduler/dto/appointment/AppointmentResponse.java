package com.example.scheduler.dto.appointment;

import lombok.Data;

import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,

        // Schedule
        Long scheduleId,
        LocalDateTime scheduleStart,
        LocalDateTime scheduleEnd,

        // Doctor
        Long doctorId,
        String doctorName,
        String doctorSpecialty,
        String doctorEmail,

        // Client
        Long clientId,
        String clientName,
        String clientEmail,

        String status,
        LocalDateTime createdAt
) {}
