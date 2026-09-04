package com.example.scheduler.dto.personal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignAndRemoveRequest {
    @NotNull
    private Long patientId;
    @NotNull
    private Long doctorId;
}
