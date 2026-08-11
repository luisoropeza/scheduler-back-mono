package com.example.scheduler.enums;

public enum ActionEvent {
    BOOKED, CONFIRMED, CANCELLED, RESCHEDULED;

    public String getDisplayName() {
        return switch (this) {
            case BOOKED -> "Reservado";
            case CONFIRMED -> "Confirmado";
            case CANCELLED -> "Cancelado";
            case RESCHEDULED -> "Reprogramado";
        };
    }
}
