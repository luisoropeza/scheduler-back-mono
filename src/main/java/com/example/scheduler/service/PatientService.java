package com.example.scheduler.service;

import com.example.scheduler.dto.patient.PatientRequest;
import com.example.scheduler.dto.patient.PatientResponse;
import com.example.scheduler.dto.personal.PersonalResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PatientService {
    Page<PatientResponse> findAll(Pageable pageable);
    PatientResponse findById(Long id);
    PatientResponse findByPhoneNumber(String phoneNumber);
    PatientResponse update(Long id, PatientRequest request, Long userId);
    void deactivate(Long id);
    List<PersonalResponse> getDoctorsOfPatient(Long patientId);
}
