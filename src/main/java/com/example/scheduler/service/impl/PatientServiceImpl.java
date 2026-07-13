package com.example.scheduler.service.impl;

import com.example.scheduler.dto.PatientRequest;
import com.example.scheduler.dto.PatientResponse;
import com.example.scheduler.dto.PersonalResponse;
import com.example.scheduler.entity.Patient;
import com.example.scheduler.exception.BusinessException;
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
    public Page<PatientResponse> findAll(Pageable pageable) {
        return patientRepository.findAll(pageable).map(patientMapper::toResponse);
    }

    @Override
    public PatientResponse findById(Long id) {
        return patientMapper.toResponse(getOrThrow(id));
    }

    @Override
    public PatientResponse findByPhoneNumber(String phoneNumber) {
        return patientMapper.toResponse(patientRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with phone number: " + phoneNumber)));
    }

    @Override
    @Transactional
    public PatientResponse update(Long id, PatientRequest request, Long userId) {
        if (!id.equals(userId))
            throw new BusinessException("This user does not authorize to update this user");
        Patient patient = getOrThrow(id);
        patientMapper.toEntityUpdated(request, patient);
        return patientMapper.toResponse(patientRepository.save(patient));
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Patient patient = getOrThrow(id);
        patient.setActive(false);
        patientRepository.save(patient);
    }

    @Override
    public List<PersonalResponse> getDoctorsOfPatient(Long patientId) {
        Patient patient = getOrThrow(patientId);
        return personalMapper.toResponseList(patient.getDoctors());
    }

    private Patient getOrThrow(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
    }
}
