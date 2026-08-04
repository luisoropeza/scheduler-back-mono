package com.example.scheduler.service.impl;

import com.example.scheduler.dto.SpecialtyRequest;
import com.example.scheduler.dto.SpecialtyResponse;
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
    public List<SpecialtyResponse> findAll() {
        return specialtyMapper.toResponseList(specialtyRepository.findAll());
    }

    @Override
    @Transactional
    public SpecialtyResponse create(SpecialtyRequest request) {
        if (specialtyRepository.existsByName(request.getName())) {
            throw new BusinessException("Specialty already exists in this clinic");
        }
        return specialtyMapper.toResponse(specialtyRepository.save(specialtyMapper.toEntity(request)));
    }
}
