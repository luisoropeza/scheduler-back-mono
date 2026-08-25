package com.example.scheduler.repository;

import com.example.scheduler.entity.Account;
import com.example.scheduler.entity.Personal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PersonalRepository extends JpaRepository<Personal, Long> {

    @Query("SELECT p FROM Personal p WHERE " +
            "p.account.role.name = 'DOCTOR' AND " +
            "(:specialtyId IS NULL OR p.specialty.id = :specialtyId) AND " +
            "(:isActive IS NULL OR p.active = :isActive)")
    Page<Personal> findAllDoctorsByFilters(@Param("specialtyId") Long specialtyId, @Param("isActive") Boolean isActive, Pageable pageable);
    @Query("SELECT p FROM Personal p WHERE " +
            "(:role IS NULL OR p.account.role.id = :roleId) AND " +
            "(:specialtyId IS NULL OR p.specialty.id = :specialtyId) AND " +
            "(:isActive IS NULL OR p.active = :isActive)")
    Page<Personal> findAllByFilters(@Param("specialtyId") Long specialtyId, @Param("isActive") Boolean isActive, @Param("roleId") Long roleId, Pageable pageable);
    @Query(value = "SELECT DISTINCT clinic_id FROM personal WHERE personal_account_id = :accountId", nativeQuery = true)
    List<String> findClinicIdsByAccountId(@Param("accountId") Long accountId);
    Optional<Personal> findByAccountId(Long accountId);
    @Query("SELECT p.id FROM Personal p WHERE p.account.id = :accountId")
    Optional<Long> findIdByAccountId(@Param("accountId") Long accountId);
    boolean existsByAccountId(Long accountId);
}
