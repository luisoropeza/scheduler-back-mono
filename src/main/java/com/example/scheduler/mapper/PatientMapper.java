package com.example.scheduler.mapper;

import com.example.scheduler.dto.PatientRegisterRequest;
import com.example.scheduler.dto.PatientRequest;
import com.example.scheduler.dto.PatientResponse;
import com.example.scheduler.entity.Patient;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    PatientResponse toResponse(Patient patient);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "doctors", ignore = true)
    @Mapping(target = "password", ignore = true)
    Patient toEntity(PatientRegisterRequest request);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "doctors", ignore = true)
    @Mapping(target = "password", ignore = true)
    void toEntityUpdated(PatientRequest request, @MappingTarget Patient patient);
    List<PatientResponse> toResponseList(List<Patient> patients);
}
