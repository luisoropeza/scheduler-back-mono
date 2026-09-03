package com.example.scheduler.repository;

import com.example.scheduler.entity.Appointment;
import com.example.scheduler.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    @Query("SELECT a FROM Appointment a WHERE " +
            "(:doctorId IS NULL OR a.schedule.doctor.id = :doctorId) AND " +
            "(:patientId IS NULL OR a.patient.id = :patientId) AND " +
            "(:status IS NULL OR a.status = :status)")
    Page<Appointment> findAllByFilters(@Param("doctorId") Long doctorId, @Param("patientId") Long patientId, @Param("status") AppointmentStatus status, Pageable pageable);

    @Query("SELECT a FROM Appointment a WHERE " +
            "(:doctorId IS NULL OR a.schedule.doctor.id = :doctorId) AND " +
            "(:patientId IS NULL OR a.patient.id = :patientId) AND " +
            "a.schedule.startTime >= :from AND a.schedule.startTime < :to " +
            "ORDER BY a.schedule.startTime ASC")
    List<Appointment> findByFiltersAndDateRange(@Param("doctorId") Long doctorId, @Param("patientId") Long patientId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
