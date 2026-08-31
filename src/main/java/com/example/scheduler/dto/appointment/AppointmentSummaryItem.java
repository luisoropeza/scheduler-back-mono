package com.example.scheduler.dto.appointment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class AppointmentSummaryItem {
    private String clientName;
    private String doctorName;
    private LocalDate appointmentDate;
    private String appointmentTime;
}
