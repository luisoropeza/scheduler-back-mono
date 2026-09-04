package com.example.scheduler.repository;

import com.example.scheduler.entity.Personal;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PersonalRepository extends JpaRepository<Personal, Long> {
    @Query("SELECT p FROM Personal p " +
            "JOIN FETCH p.account a " +
            "JOIN FETCH p.role r " +
            "JOIN FETCH p.specialty s " +
            "WHERE r.name = ERole.DOCTOR AND " +
            "(:specialtyId IS NULL OR s.id = :specialtyId) AND " +
            "(:isActive IS NULL OR p.active = :isActive)")
    Page<Personal> findAllDoctorsByFilters(@Param("specialtyId") Long specialtyId, @Param("isActive") Boolean isActive, Pageable pageable);

    @Query("SELECT p FROM Personal p " +
            "JOIN FETCH p.account a " +
            "JOIN FETCH p.role r " +
            "LEFT JOIN FETCH p.specialty s " +
            "WHERE (:roleId IS NULL OR p.role.id = :roleId) AND " +
            "(:specialtyId IS NULL OR p.specialty.id = :specialtyId) AND " +
            "(:isActive IS NULL OR p.active = :isActive)")
    Page<Personal> findAllByFilters(@Param("specialtyId") Long specialtyId, @Param("isActive") Boolean isActive, @Param("roleId") Long roleId, Pageable pageable);

    @Query("SELECT p FROM Personal p " +
            "JOIN FETCH p.account a " +
            "JOIN FETCH p.role r " +
            "WHERE a.email = :email")
    Optional<Personal> findByAccountEmail(@Param("email") String accountEmail);

    @NullMarked
    @EntityGraph(attributePaths = {"specialty", "account", "role"})
    Optional<Personal> findById(Long id);

    @EntityGraph(attributePaths = {"patients", "patients.account", "patients.role"})
    Optional<Personal>  findDoctorPatientsById(Long id);
}
