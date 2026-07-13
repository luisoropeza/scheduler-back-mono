package com.example.scheduler.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IntegrationBookingRequest {
    @NotBlank
    private String phoneNumber;
    @NotNull
    private Long scheduleId;
}
