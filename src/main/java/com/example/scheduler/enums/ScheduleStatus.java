package com.example.scheduler.enums;

public enum ScheduleStatus {
    AVAILABLE, BOOKED;

    public String getDisplayName() {
        return switch (this) {
            case AVAILABLE -> "Disponible";
            case BOOKED -> "Reservado";
        };
    }
}
