package com.example.scheduler.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PatientRequest {
    @NotBlank
    private String name;
    private String email;
    private String phoneNumber;
}
