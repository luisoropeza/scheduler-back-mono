package com.example.scheduler.service;

import com.example.scheduler.dto.ClinicRequest;
import com.example.scheduler.dto.ClinicResponse;

import java.util.List;

public interface ClinicService {
    ClinicResponse create(ClinicRequest request);

    List<ClinicResponse> findAll();
}
