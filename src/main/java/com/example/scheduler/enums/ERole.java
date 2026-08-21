package com.example.scheduler.enums;

public enum ERole {
    ADMINISTRATOR,
    DOCTOR,
    RECEPTIONIST,
    PATIENT;

    public String getDisplayName() {
        return switch (this) {
            case ADMINISTRATOR -> "Administrador";
            case DOCTOR -> "Doctor";
            case RECEPTIONIST -> "Recepcionista";
            case PATIENT -> "Paciente";
        };
    }
}
