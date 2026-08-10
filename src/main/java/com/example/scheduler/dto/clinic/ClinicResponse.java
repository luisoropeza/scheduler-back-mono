package com.example.scheduler.dto.clinic;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClinicResponse {
    private Long id;
    private String name;
    private String phoneNumber;
}
