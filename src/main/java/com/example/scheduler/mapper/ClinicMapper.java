package com.example.scheduler.mapper;

import com.example.scheduler.dto.ClinicRequest;
import com.example.scheduler.dto.ClinicResponse;
import com.example.scheduler.entity.Clinic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClinicMapper {
    List<ClinicResponse> toResponseList(List<Clinic> clinics);
    ClinicResponse toResponse(Clinic clinic);
    @Mapping(target = "id", ignore = true)
    Clinic toEntity(ClinicRequest request);
}
