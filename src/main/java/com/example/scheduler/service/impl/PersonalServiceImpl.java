package com.example.scheduler.service.impl;

import com.example.scheduler.dto.patient.PatientResponse;
import com.example.scheduler.dto.personal.AssignAndRemoveRequest;
import com.example.scheduler.dto.personal.PersonalRequest;
import com.example.scheduler.dto.personal.PersonalResponse;
import com.example.scheduler.entity.Patient;
import com.example.scheduler.entity.Personal;
import com.example.scheduler.enums.ERole;
import com.example.scheduler.exception.ForbiddenException;
import com.example.scheduler.exception.ResourceNotFoundException;
import com.example.scheduler.mapper.PatientMapper;
import com.example.scheduler.mapper.PersonalMapper;
import com.example.scheduler.repository.PatientRepository;
import com.example.scheduler.repository.PersonalRepository;
import com.example.scheduler.repository.RoleRepository;
import com.example.scheduler.repository.SpecialtyRepository;
import com.example.scheduler.service.PersonalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalServiceImpl implements PersonalService {
    private final PersonalRepository personalRepository;
    private final PatientRepository patientRepository;
    private final SpecialtyRepository specialtyRepository;
    private final RoleRepository roleRepository;
    private final PersonalMapper personalMapper;
    private final PatientMapper patientMapper;

    @Override
    public Page<PersonalResponse> findAllDoctors(Long specialtyId, Boolean isActive, Pageable pageable) {
        if(specialtyId != null)
            getSpecialtyOrThrowById(specialtyId);
        return personalRepository.findAllDoctorsByFilters(specialtyId, isActive, pageable)
                .map(personalMapper::toResponse);
    }

    @Override
    public Page<PersonalResponse> findAllPersonal(Long specialtyId, Boolean isActive, Long roleId, Pageable pageable) {
        if(specialtyId != null)
            getSpecialtyOrThrowById(specialtyId);
        if(roleId != null)
            getRoleOrThrowById(roleId);
        return personalRepository.findAllByFilters(specialtyId, isActive, roleId, pageable)
                .map(personalMapper::toResponse);
    }

    @Override
    public PersonalResponse findPersonalById(Long personalId) {
        return personalMapper.toResponse(getPersonalOrThrowById(personalId));
    }

    @Override
    @Transactional
    public PersonalResponse updatePersonalById(Long personalId, PersonalRequest request) {
        Personal personal = getPersonalOrThrowById(personalId);
        personalMapper.toEntityUpdated(request, personal);
        return personalMapper.toResponse(personalRepository.save(personal));
    }

    @Override
    @Transactional
    public void deactivatePersonalById(Long personalId) {
        Personal personal = getPersonalOrThrowById(personalId);
        personal.setActive(false);
        personalRepository.save(personal);
    }

    @Override
    @Transactional
    public void assignPatient(AssignAndRemoveRequest request, Long userId, String role) {
        Personal doctor = getPersonalPatientsOrThrowById(request.getDoctorId());
        Patient patient = getPatientOrThrowById(request.getPatientId());
        verifyDoctorPermission(role, doctor.getId(), userId);
        if (!doctor.getPatients().contains(patient)) {
            doctor.getPatients().add(patient);
            personalRepository.save(doctor);
        }
    }

    @Override
    @Transactional
    public void removePatient(AssignAndRemoveRequest request, Long userId, String role) {
        Personal doctor = getPersonalPatientsOrThrowById(request.getDoctorId());
        Patient patient = getPatientOrThrowById(request.getPatientId());
        verifyDoctorPermission(role, doctor.getId(), userId);
        if (doctor.getPatients().contains(patient)) {
            doctor.getPatients().remove(patient);
            personalRepository.save(doctor);
        }
    }

    @Override
    public List<PatientResponse> getPatientsOfDoctor(Long doctorId) {
        Personal doctor = getPersonalPatientsOrThrowById(doctorId);
        return patientMapper.toResponseList(doctor.getPatients());
    }

    @Override
    public PatientResponse findByAccountCi(String ci) {
        return null;
    }

    private Personal getPersonalOrThrowById(Long personalId) {
        return personalRepository.findById(personalId)
                .orElseThrow(() -> new ResourceNotFoundException("Personal not fount with id: " + personalId));
    }

    private Personal getPersonalPatientsOrThrowById(Long personalId) {
        return personalRepository.findDoctorPatientsById(personalId)
                .orElseThrow(() -> new ResourceNotFoundException("Personal not fount with id: " + personalId));
    }

    private void getSpecialtyOrThrowById(Long specialtyId) {
        specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with id: " + specialtyId));
    }

    private void getRoleOrThrowById(Long roleId) {
        roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
    }

    private Patient getPatientOrThrowById(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
    }

    public void verifyDoctorPermission(String role, Long accountId, Long userId) {

        if (role.equals(ERole.DOCTOR.name()) && !accountId.equals(userId))
            throw new ForbiddenException("Not authorize to do this");
    }
}
