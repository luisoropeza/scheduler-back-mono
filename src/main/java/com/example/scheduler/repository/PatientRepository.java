package com.example.scheduler.repository;

import com.example.scheduler.entity.Account;
import com.example.scheduler.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    @Query(value = "SELECT DISTINCT p.clinic_id " +
            "FROM patients p " +
            "INNER JOIN accounts a ON p.patient_account_id = a.id " +
            "WHERE a.phone_number = :phoneNumber",
            nativeQuery = true)
    List<String> findClinicIdsByPhoneNumber(@Param("phoneNumber") String phoneNumber);
    Optional<Patient> findByAccountPhoneNumber(String phoneNumber);
    Optional<Patient> findByAccountEmail(String accountEmail);
}
