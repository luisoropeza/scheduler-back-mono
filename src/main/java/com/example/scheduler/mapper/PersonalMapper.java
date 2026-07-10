package com.example.scheduler.mapper;

import com.example.scheduler.dto.PersonalRegisterRequest;
import com.example.scheduler.dto.PersonalRequest;
import com.example.scheduler.dto.PersonalResponse;
import com.example.scheduler.entity.Personal;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PersonalMapper {
    @Mapping(target = "roleName", source = "role.name")
    @Mapping(target = "specialtyName", source = "specialty.name")
    PersonalResponse toResponse(Personal personal);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "specialty", ignore = true)
    @Mapping(target = "patients", ignore = true)
    @Mapping(target = "role", ignore = true)
    Personal toEntity(PersonalRegisterRequest request);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "specialty", ignore = true)
    @Mapping(target = "patients", ignore = true)
    @Mapping(target = "role", ignore = true)
    void toEntityUpdated(PersonalRequest request, @MappingTarget Personal personal);
    List<PersonalResponse> toResponseList(List<Personal> personals);
}
