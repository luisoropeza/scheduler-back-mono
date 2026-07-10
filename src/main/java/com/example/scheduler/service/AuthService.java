package com.example.scheduler.service;

import com.example.scheduler.dto.LoginRequest;
import com.example.scheduler.dto.LoginResponse;
import com.example.scheduler.dto.PatientRegisterRequest;
import com.example.scheduler.dto.PersonalRegisterRequest;

public interface AuthService {
    LoginResponse registerPatient(PatientRegisterRequest request);
    LoginResponse loginPatient(LoginRequest request);
    LoginResponse registerPersonal(PersonalRegisterRequest request);
    LoginResponse loginPersonal(LoginRequest request);
}
