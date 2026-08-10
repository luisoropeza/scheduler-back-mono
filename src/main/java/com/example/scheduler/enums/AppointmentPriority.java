package com.example.scheduler.enums;

public enum AppointmentPriority {
    LOW, MEDIUM, HIGH;

    public String getDisplayName() {
        return switch (this) {
            case LOW -> "Low";
            case MEDIUM -> "Medium";
            case HIGH -> "High";
        };
    }
}
