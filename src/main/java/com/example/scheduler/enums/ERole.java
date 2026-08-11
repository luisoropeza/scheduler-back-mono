package com.example.scheduler.enums;

public enum ERole {
    ADMINISTRATOR,
    DOCTOR,
    SUPERVISOR,
    RECEPTIONIST,
    PATIENT;

    public String getDisplayName() {
        return switch (this) {
            case ADMINISTRATOR -> "Administrador";
            case DOCTOR -> "Doctor";
            case SUPERVISOR -> "Supervisor";
            case RECEPTIONIST -> "Recepcionista";
            case PATIENT -> "Paciente";
        };
    }
}
