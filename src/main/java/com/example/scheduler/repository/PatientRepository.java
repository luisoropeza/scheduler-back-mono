package com.example.scheduler.repository;

import com.example.scheduler.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByAccountPhoneNumber(String phoneNumber);
    Optional<Patient> findByAccountEmail(String accountEmail);
}
