package com.example.scheduler.service;

import com.example.scheduler.event.AppointmentEvent;

public interface NotificationService {
    void handleEvent(AppointmentEvent event);
}
