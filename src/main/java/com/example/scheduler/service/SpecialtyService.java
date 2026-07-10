package com.example.scheduler.service;

import com.example.scheduler.dto.SpecialtyResponse;

import java.util.List;

public interface SpecialtyService {
    List<SpecialtyResponse> findAll();
}
