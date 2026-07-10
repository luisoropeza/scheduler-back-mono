package com.example.scheduler.service.impl;

import com.example.scheduler.dto.ScheduleRequest;
import com.example.scheduler.dto.ScheduleResponse;
import com.example.scheduler.entity.Personal;
import com.example.scheduler.entity.Schedule;
import com.example.scheduler.enums.ScheduleStatus;
import com.example.scheduler.exception.BusinessException;
import com.example.scheduler.exception.ResourceNotFoundException;
import com.example.scheduler.mapper.ScheduleMapper;
import com.example.scheduler.repository.PersonalRepository;
import com.example.scheduler.repository.ScheduleRepository;
import com.example.scheduler.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleServiceImpl implements ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final PersonalRepository personalRepository;
    private final ScheduleMapper scheduleMapper;

    @Override
    public Page<ScheduleResponse> findAll(
            Long doctorId,
            Long specialtyId,
            ScheduleStatus status,
            LocalDateTime after,
            Pageable pageable) {
        if (doctorId != null)
            getActiveDoctorOrThrow(doctorId);
        return scheduleRepository
                .findAllByFilters(
                        doctorId,
                        specialtyId,
                        status == null ? ScheduleStatus.AVAILABLE : status,
                        after == null ? LocalDateTime.now() : after,
                        pageable)
                .map(scheduleMapper::toResponse);
    }

    @Override
    public ScheduleResponse findById(Long id) {
        return scheduleMapper.toResponse(getScheduleOrThrow(id));
    }

    @Override
    @Transactional
    public ScheduleResponse create(Long doctorId, ScheduleRequest request) {
        Personal doctor = getActiveDoctorOrThrow(doctorId);
        validateSlotTimes(request);
        return scheduleMapper.toResponse(scheduleRepository.save(buildSchedule(doctor, request)));
    }

    @Override
    @Transactional
    public List<ScheduleResponse> createBatch(Long doctorId, List<ScheduleRequest> requests) {
        Personal doctor = getActiveDoctorOrThrow(doctorId);
        requests.forEach(this::validateSlotTimes);
        return scheduleMapper.toResponseList(scheduleRepository.saveAll(requests.stream().map(r -> buildSchedule(doctor, r)).toList()));
    }

    @Override
    @Transactional
    public void delete(Long scheduleId, Long doctorId) {
        Schedule schedule = getScheduleOrThrow(scheduleId);
        if (!schedule.getDoctor().getId().equals(doctorId))
            throw new BusinessException("Not authorized to delete this schedule slot");
        if (schedule.getStatus() == ScheduleStatus.BOOKED)
            throw new BusinessException("Cannot delete a booked schedule slot");
        scheduleRepository.delete(schedule);
    }

    private Personal getActiveDoctorOrThrow(Long doctorId) {
        Personal doctor = personalRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Personal not found with id: " + doctorId));
        if (!doctor.isActive()) throw new BusinessException("Doctor is not active");
        return doctor;
    }

    private Schedule getScheduleOrThrow(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));
    }

    private void validateSlotTimes(ScheduleRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime()))
            throw new BusinessException("End time must be after start time");
    }

    private Schedule buildSchedule(Personal doctor, ScheduleRequest request) {
        return Schedule.builder()
                .doctor(doctor)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();
    }
}
