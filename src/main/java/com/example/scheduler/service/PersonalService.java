package com.example.scheduler.service;

import com.example.scheduler.dto.patient.PatientResponse;
import com.example.scheduler.dto.personal.AssignAndRemoveRequest;
import com.example.scheduler.dto.personal.PersonalRequest;
import com.example.scheduler.dto.personal.PersonalResponse;
import com.example.scheduler.entity.Personal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PersonalService {
    Page<PersonalResponse> findAllDoctors(Long specialtyId, Boolean isActive, Pageable pageable);
    Page<PersonalResponse> findAllPersonal(Long specialtyId, Boolean isActive, Long roleId, Pageable pageable);
    PersonalResponse findPersonalById(Long personalId);
    PersonalResponse updatePersonalById(Long personalId, PersonalRequest request);
    void deactivatePersonalById(Long personalId);
    void assignPatient(AssignAndRemoveRequest request, Long userId, String role);
    void removePatient(AssignAndRemoveRequest request, Long userId, String role);
    List<PatientResponse> getPatientsOfDoctor(Long doctorId);
    Personal findBySelf(Long accountId);
}
