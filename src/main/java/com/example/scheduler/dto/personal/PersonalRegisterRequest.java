package com.example.scheduler.dto.personal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PersonalRegisterRequest(
        @NotBlank
        String name,
        @NotBlank
        @Email
        String email,
        @NotBlank
        String ci,
        @NotBlank
        @Size(min = 8)
        String password,
        @NotNull
        Long roleId,
        @NotNull
        Long specialtyId
) {}
