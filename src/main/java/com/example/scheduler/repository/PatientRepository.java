package com.example.scheduler.repository;

import com.example.scheduler.entity.Account;
import com.example.scheduler.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    @Query(value = "SELECT DISTINCT clinic_id FROM patients WHERE patient_account_id = :accountId", nativeQuery = true)
    List<String> findClinicIdsByAccountId(@Param("accountId") Long accountId);
    Optional<Patient> findByAccountId(Long accountId);
    Optional<Patient> findByAccountPhoneNumber(String phoneNumber);
    boolean existsByAccountId(Long accountId);
    @Query("SELECT p.id FROM Patient p WHERE p.account.id = :accountId")
    Optional<Long> findIdByAccountId(@Param("accountId") Long accountId);
}
