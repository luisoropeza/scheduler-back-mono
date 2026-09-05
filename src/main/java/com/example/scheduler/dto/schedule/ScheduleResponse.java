package com.example.scheduler.dto.schedule;

import java.time.LocalDateTime;

public record ScheduleResponse(
        Long id,

        // Doctor
        Long doctorId,
        String doctorName,
        String doctorSpecialty,
        String doctorEmail,

        LocalDateTime startTime,
        LocalDateTime endTime,
        String status
) {}
