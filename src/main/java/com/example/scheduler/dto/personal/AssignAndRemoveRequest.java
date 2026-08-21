package com.example.scheduler.dto.personal;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignAndRemoveRequest {
    @NotBlank
    private Long patientId;
    @NotBlank
    private Long doctorId;
}
