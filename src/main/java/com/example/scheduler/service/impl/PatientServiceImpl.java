package com.example.scheduler.service.impl;

import com.example.scheduler.dto.patient.PatientRequest;
import com.example.scheduler.dto.patient.PatientResponse;
import com.example.scheduler.dto.personal.PersonalResponse;
import com.example.scheduler.entity.Patient;
import com.example.scheduler.exception.ResourceNotFoundException;
import com.example.scheduler.mapper.PatientMapper;
import com.example.scheduler.mapper.PersonalMapper;
import com.example.scheduler.repository.PatientRepository;
import com.example.scheduler.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientServiceImpl implements PatientService {
    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final PersonalMapper personalMapper;

    @Override
    public Page<PatientResponse> findAllPatients(Pageable pageable) {
        return patientRepository.findAll(pageable).map(patientMapper::toResponse);
    }

    @Override
    public PatientResponse findPatientById(Long patientId) {
        return patientMapper.toResponse(getPatientOrThrow(patientId));
    }

    @Override
    public PatientResponse findPatientByPhoneNumber(String phoneNumber) {
        return patientMapper.toResponse(patientRepository.findByAccountPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el paciente con el numero telefonico: " + phoneNumber)));
    }

    @Override
    @Transactional
    public PatientResponse updatePatientById(Long patientId, PatientRequest request) {
        Patient patient = getPatientOrThrow(patientId);
        patientMapper.toEntityUpdated(request, patient);
        return patientMapper.toResponse(patientRepository.save(patient));
    }

    @Override
    @Transactional
    public void deactivatePatientById(Long patientId) {
        Patient patient = getPatientOrThrow(patientId);
        patient.setActive(false);
        patientRepository.save(patient);
    }

    @Override
    public List<PersonalResponse> getDoctorsOfPatient(Long patientId) {
        Patient patient = getPatientOrThrow(patientId);
        return personalMapper.toResponseList(patient.getDoctors());
    }

    @Override
    public Patient findBySelf(Long accountId) {
        return patientRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el paciente con el accountId: " + accountId));
    }

    private Patient getPatientOrThrow(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el paciente con el id: " + id));
    }
}
