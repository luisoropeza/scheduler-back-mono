package com.example.scheduler.service;

import com.example.scheduler.dto.login.LoginRequest;
import com.example.scheduler.dto.login.LoginResponse;
import com.example.scheduler.dto.patient.PatientRegisterRequest;
import com.example.scheduler.dto.personal.PersonalRegisterRequest;

public interface AuthService {
    void registerPatient(PatientRegisterRequest request);
    LoginResponse loginPatient(LoginRequest request);
    void registerPersonal(PersonalRegisterRequest request);
    LoginResponse loginPersonal(LoginRequest request);
}
