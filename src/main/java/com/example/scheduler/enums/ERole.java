package com.example.scheduler.enums;

public enum ERole {
    ADMINISTRATOR,
    DOCTOR,
    ASSISTANT,
    PATIENT;

    public String getDisplayName() {
        return switch (this) {
            case ADMINISTRATOR -> "Administrador";
            case DOCTOR -> "Doctor";
            case ASSISTANT -> "Asistente";
            case PATIENT -> "Paciente";
        };
    }
}
