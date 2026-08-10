package com.example.scheduler.dto.personal;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PersonalRequest {
    @NotBlank
    private String name;
    private String email;
    private Long specialtyId;
}
