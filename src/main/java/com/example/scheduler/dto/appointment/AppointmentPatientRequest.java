package com.example.scheduler.dto.appointment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentPatientRequest {
    @NotNull
    private Long scheduleId;
}
