package com.example.scheduler.service;

import com.example.scheduler.dto.ScheduleRequest;
import com.example.scheduler.dto.ScheduleResponse;
import com.example.scheduler.enums.ScheduleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleService {
    Page<ScheduleResponse> findAll(Long doctorId, Long specialtyId, ScheduleStatus status, LocalDateTime after, Pageable pageable);
    ScheduleResponse findById(Long id);
    ScheduleResponse create(Long doctorId, ScheduleRequest request);
    List<ScheduleResponse> createBatch(Long doctorId, List<ScheduleRequest> requests);
    void delete(Long scheduleId, Long doctorId);
}
