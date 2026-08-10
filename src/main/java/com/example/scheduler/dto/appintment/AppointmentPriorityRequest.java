package com.example.scheduler.dto.appintment;

import com.example.scheduler.enums.AppointmentPriority;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentPriorityRequest {
    @NotNull
    private AppointmentPriority priority;
}
