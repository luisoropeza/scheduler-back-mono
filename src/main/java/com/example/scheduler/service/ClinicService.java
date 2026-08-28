package com.example.scheduler.service;

import com.example.scheduler.dto.clinic.ClinicRequest;
import com.example.scheduler.dto.clinic.ClinicResponse;

import java.util.List;

public interface ClinicService {
    ClinicResponse createClinic(ClinicRequest request);
    List<ClinicResponse> findAllClinics();
    List<ClinicResponse> findClinicsByPatientPhoneNumber(String phoneNumber);
}
