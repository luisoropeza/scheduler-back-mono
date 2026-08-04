package com.example.scheduler.repository;

import com.example.scheduler.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByAccount_Id(Long accountId);
    Optional<Patient> findByAccount_PhoneNumber(String phoneNumber);
    boolean existsByAccount_Id(Long accountId);

    @Query(value = "SELECT DISTINCT clinic_id FROM patients WHERE patient_account_id = :accountId", nativeQuery = true)
    List<String> findClinicIdsByAccountId(@Param("accountId") Long accountId);
}
