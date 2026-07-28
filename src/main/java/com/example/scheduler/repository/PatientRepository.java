package com.example.scheduler.repository;

import com.example.scheduler.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByAccount_Id(Long accountId);
    Optional<Patient> findByAccount_PhoneNumber(String phoneNumber);
}
