package com.example.scheduler.mapper;

import com.example.scheduler.dto.RoleResponse;
import com.example.scheduler.entity.Role;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    List<RoleResponse> toResponseList(List<Role> roles);
}
