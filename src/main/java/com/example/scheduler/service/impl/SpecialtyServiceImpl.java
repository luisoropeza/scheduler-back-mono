package com.example.scheduler.service.impl;

import com.example.scheduler.dto.specialty.SpecialtyRequest;
import com.example.scheduler.dto.specialty.SpecialtyResponse;
import com.example.scheduler.exception.BusinessException;
import com.example.scheduler.mapper.SpecialtyMapper;
import com.example.scheduler.repository.SpecialtyRepository;
import com.example.scheduler.service.SpecialtyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpecialtyServiceImpl implements SpecialtyService {
    private final SpecialtyRepository specialtyRepository;
    private final SpecialtyMapper specialtyMapper;

    @Override
    public List<SpecialtyResponse> findAllSpecialties() {
        return specialtyMapper.toResponseList(specialtyRepository.findAll());
    }

    @Override
    @Transactional
    public SpecialtyResponse createSpecialty(SpecialtyRequest request) {
        if (specialtyRepository.existsByName(request.getName())) {
            throw new BusinessException("Esta especialidad ya existe en esta clinica");
        }
        return specialtyMapper.toResponse(specialtyRepository.save(specialtyMapper.toEntity(request)));
    }
}
