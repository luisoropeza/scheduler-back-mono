package com.example.scheduler.repository;

import com.example.scheduler.entity.Schedule;
import com.example.scheduler.enums.ScheduleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    @Query("SELECT s FROM Schedule s WHERE " +
            "(:doctorId IS NULL OR s.doctor.id = :doctorId) AND " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(cast(:after as localdatetime) IS NULL OR s.startTime > :after) AND " +
            "(:specialtyId IS NULL OR s.doctor.specialty.id = :specialtyId)")
    Page<Schedule> findAllByFilters(
            @Param("doctorId") Long doctorId,
            @Param("specialtyId") Long specialtyId,
            @Param("status") ScheduleStatus status,
            @Param("after") LocalDateTime after,
            Pageable pageable
    );
}
