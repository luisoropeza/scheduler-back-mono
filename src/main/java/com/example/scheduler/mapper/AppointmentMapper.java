package com.example.scheduler.mapper;

import com.example.scheduler.dto.appointment.AppointmentResponse;
import com.example.scheduler.entity.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {
    @Mapping(target = "scheduleId", source = "schedule.id")
    @Mapping(target = "scheduleStart", source = "schedule.startTime")
    @Mapping(target = "scheduleEnd", source = "schedule.endTime")
    @Mapping(target = "doctorId", source = "schedule.doctor.id")
    @Mapping(target = "doctorName", source = "schedule.doctor.account.name")
    @Mapping(target = "doctorSpecialty", source = "schedule.doctor.specialty.name")
    @Mapping(target = "doctorEmail", source = "schedule.doctor.account.email")
    @Mapping(target = "clientId", source = "patient.id")
    @Mapping(target = "clientName", source = "patient.account.name")
    @Mapping(target = "clientEmail", source = "patient.account.email")
    @Mapping(target = "status", expression = "java(appointment.getStatus().getDisplayName())")
    AppointmentResponse toResponse(Appointment appointment);
}
