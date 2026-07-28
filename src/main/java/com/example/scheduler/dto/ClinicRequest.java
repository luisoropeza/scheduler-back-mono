package com.example.scheduler.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClinicRequest {
    @NotNull
    String name;

    @NotNull
    String phoneNumber;

    @NotBlank
    private String adminName;
    @NotBlank
    @Email
    private String adminEmail;
    @NotBlank
    @Size(min = 8)
    private String adminPassword;
}
