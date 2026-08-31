package com.example.scheduler.dto.clinic;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClinicCreatedResponse {
    private Long id;
    private String name;
    private String phoneNumber;
    private Long adminPersonalId;
    private String adminEmail;
}
