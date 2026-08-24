package com.example.scheduler.service;

import com.example.scheduler.dto.patient.PatientRequest;
import com.example.scheduler.dto.patient.PatientResponse;
import com.example.scheduler.dto.personal.PersonalResponse;
import com.example.scheduler.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PatientService {
    Page<PatientResponse> findAllPatients(Pageable pageable);
    PatientResponse findPatientById(Long patientId);
    PatientResponse findPatientByPhoneNumber(String phoneNumber);
    PatientResponse updatePatientById(Long patientId, PatientRequest request);
    PatientResponse updatePatientByAccountId(Long accountId, PatientRequest request);
    void deactivatePatientById(Long patientId);
    List<PersonalResponse> getDoctorsOfPatient(Long patientId);
    Patient findByAccountId(Long accountId);
}
