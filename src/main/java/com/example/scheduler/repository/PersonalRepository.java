package com.example.scheduler.repository;

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
    Page<Personal> findAllByFilters(@Param("specialtyId") Long specialtyId, @Param("isActive") Boolean isActive, Pageable pageable);
    Optional<Personal> findByAccount_Id(Long accountId);
    boolean existsByAccount_Id(Long accountId);

    @Query(value = "SELECT DISTINCT clinic_id FROM personal WHERE personal_account_id = :accountId", nativeQuery = true)
    List<String> findClinicIdsByAccountId(@Param("accountId") Long accountId);
}
