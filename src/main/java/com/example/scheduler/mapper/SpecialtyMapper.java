package com.example.scheduler.mapper;

import com.example.scheduler.dto.SpecialtyResponse;
import com.example.scheduler.entity.Specialty;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SpecialtyMapper {
    List<SpecialtyResponse> toResponseList(List<Specialty> specialties);
}
