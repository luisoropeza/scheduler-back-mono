package com.example.scheduler.repository;

import com.example.scheduler.entity.Appointment;
import com.example.scheduler.enums.AppointmentStatus;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.schedule s " +
            "JOIN FETCH s.doctor d " +
            "JOIN FETCH d.account ad " +
            "JOIN FETCH d.specialty sp " +
            "JOIN FETCH a.patient p " +
            "JOIN FETCH p.account ap " +
            "WHERE (:doctorId IS NULL OR a.schedule.doctor.id = :doctorId) AND " +
            "(:patientId IS NULL OR a.patient.id = :patientId) AND " +
            "(:status IS NULL OR a.status = :status)")
    Page<Appointment> findAllByFilters(@Param("doctorId") Long doctorId, @Param("patientId") Long patientId, @Param("status") AppointmentStatus status, Pageable pageable);

    @Query("SELECT a FROM Appointment a  " +
            "JOIN FETCH a.schedule s " +
            "JOIN FETCH s.doctor d " +
            "JOIN FETCH d.account ad " +
            "JOIN FETCH a.patient p " +
            "JOIN FETCH p.account ap " +
            "WHERE (:doctorId IS NULL OR a.schedule.doctor.id = :doctorId) AND " +
            "(:patientId IS NULL OR a.patient.id = :patientId) AND " +
            "a.schedule.startTime >= :from AND a.schedule.startTime < :to " +
            "ORDER BY a.schedule.startTime ASC")
    List<Appointment> findByFiltersAndDateRange(@Param("doctorId") Long doctorId, @Param("patientId") Long patientId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @NullMarked
    @EntityGraph(attributePaths = {"schedule.doctor.specialty", "schedule.doctor.account", "patient.account"})
    Optional<Appointment> findById(Long id);
}
