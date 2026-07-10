package com.example.scheduler.service;

import com.example.scheduler.dto.PatientResponse;
import com.example.scheduler.dto.PersonalRequest;
import com.example.scheduler.dto.PersonalResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PersonalService {
    Page<PersonalResponse> findAll(Long specialtyId, Boolean isActive, Pageable pageable);
    PersonalResponse findById(Long id);
    PersonalResponse update(Long id, PersonalRequest request);
    void deactivate(Long id);
    void assignPatient(Long doctorId, Long patientId, Long userId, String role);
    void removePatient(Long doctorId, Long patientId, Long userId, String role);
    List<PatientResponse> getPatientsOfDoctor(Long doctorId);
}
