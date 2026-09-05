package com.example.scheduler.dto.schedule;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ScheduleRequest(
        @NotNull
        @Future
        LocalDateTime startTime,
        @NotNull
        @Future
        LocalDateTime endTime
) {}
