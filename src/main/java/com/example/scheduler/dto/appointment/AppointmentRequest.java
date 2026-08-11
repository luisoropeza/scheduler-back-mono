package com.example.scheduler.dto.appointment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentRequest {
    @NotNull
    private Long scheduleId;
    @NotNull
    private Long clientId;
}
