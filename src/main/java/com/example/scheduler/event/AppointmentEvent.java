package com.example.scheduler.event;

import com.example.scheduler.entity.Appointment;
import com.example.scheduler.enums.ActionEvent;

public record AppointmentEvent(
        Appointment appointment,
        ActionEvent action,
        String actorRole
) {}
