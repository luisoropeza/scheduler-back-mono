package com.example.scheduler.service;

import com.example.scheduler.dto.schedule.ScheduleRequest;
import com.example.scheduler.dto.schedule.ScheduleResponse;
import com.example.scheduler.enums.ScheduleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleService {
    Page<ScheduleResponse> findAllSchedules(Long doctorId, Long specialtyId, ScheduleStatus status, LocalDateTime after, Pageable pageable);
    ScheduleResponse findScheduleById(Long scheduleId, Long userId, String role);
    ScheduleResponse createSchedule(Long doctorId, ScheduleRequest request);
    List<ScheduleResponse> createSchedulesBatch(Long doctorId, List<ScheduleRequest> requests);
    void deleteSchedule(Long doctorId, Long scheduleId);
}
