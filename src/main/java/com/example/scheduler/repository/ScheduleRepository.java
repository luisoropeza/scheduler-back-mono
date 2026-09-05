package com.example.scheduler.repository;

import com.example.scheduler.entity.Schedule;
import com.example.scheduler.enums.ScheduleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    @Query("SELECT s FROM Schedule s " +
            "JOIN FETCH s.doctor d " +
            "JOIN FETCH d.specialty p  " +
            "JOIN FETCH d.account a WHERE" +
            "(:doctorId IS NULL OR d.id = :doctorId) AND " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(cast(:after as localdatetime) IS NULL OR s.startTime > :after) AND " +
            "(:specialtyId IS NULL OR p.id = :specialtyId)")
    Page<Schedule> findAllByFilters(
            @Param("doctorId") Long doctorId,
            @Param("specialtyId") Long specialtyId,
            @Param("status") ScheduleStatus status,
            @Param("after") LocalDateTime after,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"doctor.specialty", "doctor.account"})
    Optional<Schedule> findScheduleById(Long id);
}
