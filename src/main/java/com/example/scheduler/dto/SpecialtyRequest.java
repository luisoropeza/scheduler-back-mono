package com.example.scheduler.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SpecialtyRequest {
    @NotBlank
    private String name;
}
