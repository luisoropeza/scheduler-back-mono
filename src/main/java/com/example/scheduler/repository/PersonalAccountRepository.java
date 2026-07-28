package com.example.scheduler.repository;

import com.example.scheduler.entity.PersonalAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonalAccountRepository extends JpaRepository<PersonalAccount, Long> {
    Optional<PersonalAccount> findByEmail(String email);
}
