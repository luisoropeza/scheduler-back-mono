package com.example.scheduler.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentRequest {
    @NotNull
    private Long scheduleId;
    @NotNull
    private Long clientId;
}
