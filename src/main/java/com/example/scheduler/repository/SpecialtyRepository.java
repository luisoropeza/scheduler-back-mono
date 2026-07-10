package com.example.scheduler.repository;

import com.example.scheduler.entity.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {
    Optional<Specialty> findByNameIgnoreCase(String specialty);
}
