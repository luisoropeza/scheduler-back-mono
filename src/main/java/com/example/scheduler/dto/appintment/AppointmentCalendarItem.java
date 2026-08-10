package com.example.scheduler.dto.appintment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppointmentCalendarItem {
    private String clientName;
    private String doctorName;
    private String appointmentTime;
    private String priority;
}
