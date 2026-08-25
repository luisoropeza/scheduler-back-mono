package com.example.scheduler.service.impl;

import com.example.scheduler.dto.schedule.ScheduleRequest;
import com.example.scheduler.dto.schedule.ScheduleResponse;
import com.example.scheduler.entity.Personal;
import com.example.scheduler.entity.Schedule;
import com.example.scheduler.enums.ERole;
import com.example.scheduler.enums.ScheduleStatus;
import com.example.scheduler.exception.BusinessException;
import com.example.scheduler.exception.ForbiddenException;
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
    public Page<ScheduleResponse> findAllSchedules(
            Long doctorId,
            Long specialtyId,
            ScheduleStatus status,
            LocalDateTime after,
            Pageable pageable) {
        if (doctorId != null)
            getActiveDoctorOrThrowById(doctorId);
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
    public ScheduleResponse findScheduleById(Long scheduleId, Long accountId, String role) {
        if(role.equals(ERole.DOCTOR.name())){
            getActiveDoctorOrThrowByAccountId(accountId);
            Schedule schedule = getScheduleOrThrowById(scheduleId);
            verifyDoctorPermission(schedule, accountId);
            return scheduleMapper.toResponse(schedule);
        }
        return scheduleMapper.toResponse(getScheduleOrThrowById(scheduleId));
    }

    @Override
    @Transactional
    public ScheduleResponse createSchedule(Long accountId, ScheduleRequest request) {
        Personal doctor = getActiveDoctorOrThrowByAccountId(accountId);
        validateSlotTimes(request);
        return scheduleMapper.toResponse(scheduleRepository.save(buildSchedule(doctor, request)));
    }

    @Override
    @Transactional
    public List<ScheduleResponse> createSchedulesBatch(Long accountId, List<ScheduleRequest> requests) {
        Personal doctor = getActiveDoctorOrThrowByAccountId(accountId);
        requests.forEach(this::validateSlotTimes);
        return scheduleMapper.toResponseList(scheduleRepository.saveAll(requests.stream().map(r -> buildSchedule(doctor, r)).toList()));
    }

    @Override
    @Transactional
    public void deleteScheduleById(Long scheduleId, Long accountId) {
        getActiveDoctorOrThrowByAccountId(accountId);
        Schedule schedule = getScheduleOrThrowById(scheduleId);
        verifyDoctorPermission(schedule, accountId);
        if (schedule.getStatus() == ScheduleStatus.BOOKED)
            throw new BusinessException("Can't remove schedule already booked");
        scheduleRepository.delete(schedule);
    }

    private Personal getActiveDoctorOrThrowByAccountId(Long accountId) {
        Personal doctor = personalRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with accountId: " + accountId));
        if (!doctor.isActive()) throw new BusinessException("This doctor is not active");
        return doctor;
    }

    private void getActiveDoctorOrThrowById(Long personalId) {
        Personal doctor = personalRepository.findById(personalId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + personalId));
        if (!doctor.isActive()) throw new BusinessException("This doctor is not active");
    }

    private Schedule getScheduleOrThrowById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + scheduleId));
    }

    private void verifyDoctorPermission(Schedule schedule, Long personalId) {
        if(!schedule.getDoctor().getAccount().getId().equals(personalId)){
            throw new ForbiddenException("Can't remove a schedule that doesn't belong to you.");
        }
    }

    private void validateSlotTimes(ScheduleRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime()))
            throw new BusinessException("The end time must be after the start time");
    }

    private Schedule buildSchedule(Personal doctor, ScheduleRequest request) {
        return Schedule.builder()
                .doctor(doctor)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();
    }
}
