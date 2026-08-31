package com.example.scheduler.service;

import com.example.scheduler.dto.schedule.ScheduleRequest;
import com.example.scheduler.dto.schedule.ScheduleResponse;
import com.example.scheduler.enums.ScheduleStatus;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StreamUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleService {
    Page<ScheduleResponse> findAllSchedules(Long doctorId, Long specialtyId, ScheduleStatus status, LocalDateTime after, Pageable pageable);
    ScheduleResponse findScheduleById(Long scheduleId, Long userId, String role);
    ScheduleResponse createSchedule(Long userId, ScheduleRequest request);
    List<ScheduleResponse> createSchedulesBatch(Long userId, List<ScheduleRequest> requests);
    void deleteScheduleById(Long scheduleId, Long userId);
}
