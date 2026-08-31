package com.example.scheduler.dto.specialty;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SpecialtyRequest {
    @NotBlank
    private String name;
}
