package com.example.scheduler.service.impl;

import com.example.scheduler.dto.schedule.ScheduleRequest;
import com.example.scheduler.dto.schedule.ScheduleResponse;
import com.example.scheduler.entity.Personal;
import com.example.scheduler.entity.Schedule;
import com.example.scheduler.enums.ERole;
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
    public Page<ScheduleResponse> findAllSchedules(
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
    public ScheduleResponse findScheduleById(Long scheduleId, Long userId, String role) {
        if(role.equals(ERole.DOCTOR.name()))
            return scheduleMapper.toResponse(getScheduleOrThrow(userId,  scheduleId));
        return scheduleMapper.toResponse(getScheduleOrThrow(scheduleId));
    }

    @Override
    @Transactional
    public ScheduleResponse createSchedule(Long doctorId, ScheduleRequest request) {
        Personal doctor = getActiveDoctorOrThrow(doctorId);
        validateSlotTimes(request);
        return scheduleMapper.toResponse(scheduleRepository.save(buildSchedule(doctor, request)));
    }

    @Override
    @Transactional
    public List<ScheduleResponse> createSchedulesBatch(Long doctorId, List<ScheduleRequest> requests) {
        Personal doctor = getActiveDoctorOrThrow(doctorId);
        requests.forEach(this::validateSlotTimes);
        return scheduleMapper.toResponseList(scheduleRepository.saveAll(requests.stream().map(r -> buildSchedule(doctor, r)).toList()));
    }

    @Override
    @Transactional
    public void deleteSchedule(Long doctorId, Long scheduleId) {
        getActiveDoctorOrThrow(doctorId);
        Schedule schedule = getScheduleOrThrow(doctorId, scheduleId);
        if (schedule.getStatus() == ScheduleStatus.BOOKED)
            throw new BusinessException("Cannot deleteSchedule a booked schedule slot");
        scheduleRepository.delete(schedule);
    }

    private Personal getActiveDoctorOrThrow(Long doctorId) {
        Personal doctor = personalRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro a este usuario con el id: " + doctorId));
        if (!doctor.isActive()) throw new BusinessException("Este doctor no esta activo");
        return doctor;
    }

    private Schedule getScheduleOrThrow(Long doctorId, Long scheduleId) {
        return scheduleRepository.findByIdAndDoctorId(scheduleId, doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el horario con el id: " + scheduleId + "para el doctor con el id: " + doctorId));
    }

    private Schedule getScheduleOrThrow(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el horario con el id: " + scheduleId));
    }

    private void validateSlotTimes(ScheduleRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime()))
            throw new BusinessException("La hora de finalizacion debe ser despues de la hora de inicio");
    }

    private Schedule buildSchedule(Personal doctor, ScheduleRequest request) {
        return Schedule.builder()
                .doctor(doctor)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();
    }
}
