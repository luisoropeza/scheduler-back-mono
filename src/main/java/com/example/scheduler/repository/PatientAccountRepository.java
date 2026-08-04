package com.example.scheduler.repository;

import com.example.scheduler.entity.PatientAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientAccountRepository extends JpaRepository<PatientAccount, Long> {
    Optional<PatientAccount> findByEmail(String email);
    Optional<PatientAccount> findByPhoneNumber(String phoneNumber);
}
