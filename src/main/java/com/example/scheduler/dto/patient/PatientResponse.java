package com.example.scheduler.dto.patient;

import lombok.Data;

@Data
public class PatientResponse {
    private Long id;
    private Long accountId;
    private String name;
    private String email;
    private String phoneNumber;
    private boolean active;
}
