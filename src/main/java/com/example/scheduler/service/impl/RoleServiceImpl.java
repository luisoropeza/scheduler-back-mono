package com.example.scheduler.service.impl;

import com.example.scheduler.dto.RoleResponse;
import com.example.scheduler.mapper.RoleMapper;
import com.example.scheduler.repository.RoleRepository;
import com.example.scheduler.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    public List<RoleResponse> findAll() {
        return roleMapper.toResponseList(roleRepository.findAll());
    }
}
