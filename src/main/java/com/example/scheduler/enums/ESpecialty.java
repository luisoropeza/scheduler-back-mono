package com.example.scheduler.enums;

public enum ESpecialty {
    DEFAULT;

    public String getDisplayName() {
        return switch (this) {
            case DEFAULT -> "None";
        };
    }
}
