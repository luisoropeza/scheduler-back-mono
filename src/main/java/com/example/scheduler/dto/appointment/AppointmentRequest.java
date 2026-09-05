package com.example.scheduler.dto.appointment;

import jakarta.validation.constraints.NotNull;

public record AppointmentRequest(
        @NotNull
        Long scheduleId,
        @NotNull
        Long patientId
) {}
