package com.example.scheduler.repository;

import com.example.scheduler.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByAccountPhoneNumber(String phoneNumber);
    @Query("SELECT p FROM Patient p " +
            "JOIN FETCH p.account a " +
            "JOIN FETCH p.role r " +
            "WHERE a.email = :email")
    Optional<Patient> findByAccountEmail(@Param("email") String accountEmail);
}
