package com.example.scheduler.dto.appintment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class AppointmentBoardItem {
    private String clientName;
    private String doctorName;
    private LocalDate appointmentDate;
    private String appointmentTime;
    private String priority;
}
