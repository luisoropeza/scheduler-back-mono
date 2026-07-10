package com.example.scheduler.mapper;

import com.example.scheduler.dto.ScheduleResponse;
import com.example.scheduler.entity.Schedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {
    @Mapping(target = "doctorId", source = "doctor.id")
    @Mapping(target = "doctorName", source = "doctor.name")
    @Mapping(target = "doctorSpecialty", source = "doctor.specialty.name")
    @Mapping(target = "doctorEmail", source = "doctor.email")
    ScheduleResponse toResponse(Schedule schedule);
    List<ScheduleResponse> toResponseList(List<Schedule> schedules);
}
