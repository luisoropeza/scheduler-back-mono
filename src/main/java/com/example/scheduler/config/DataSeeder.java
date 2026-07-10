package com.example.scheduler.config;

import com.example.scheduler.entity.*;
import com.example.scheduler.enums.AppointmentStatus;
import com.example.scheduler.enums.ScheduleStatus;
import com.example.scheduler.repository.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {
    private final SpecialtyRepository specialtyRepository;
    private final RoleRepository roleRepository;
    private final PersonalRepository personalRepository;
    private final PatientRepository patientRepository;
    private final ScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        if (specialtyRepository.count() > 0) return;

        Specialty gm = specialtyRepository.save(Specialty.builder().name("General Medicine").build());
        Specialty dent = specialtyRepository.save(Specialty.builder().name("Dentistry").build());
        Specialty peds = specialtyRepository.save(Specialty.builder().name("Pediatrics").build());

        Role doctor = roleRepository.save(Role.builder().name("DOCTOR").build());
        Role receptionist = roleRepository.save(Role.builder().name("RECEPTIONIST").build());

        String pwd = passwordEncoder.encode("password123");
        Personal ana = personalRepository.save(Personal.builder().name("Dr. Ana García").email("ana.garcia@clinic.com").password(pwd).role(doctor).specialty(gm).build());
        Personal carlos = personalRepository.save(Personal.builder().name("Dr. Carlos Méndez").email("carlos.mendez@clinic.com").password(pwd).role(doctor).specialty(dent).build());
        Personal laura = personalRepository.save(Personal.builder().name("Dr. Laura Torres").email("laura.torres@clinic.com").password(pwd).role(doctor).specialty(peds).build());
        personalRepository.save(Personal.builder().name("Maria Ramos").email("maria.ramos@clinic.com").password(pwd).role(receptionist).build());

        Patient john = patientRepository.save(Patient.builder().name("John Smith").email("john.smith@email.com").phoneNumber("+1-555-1001").password(pwd).build());
        Patient maria = patientRepository.save(Patient.builder().name("María López").email("maria.lopez@email.com").phoneNumber("+1-555-1002").password(pwd).build());
        Patient james = patientRepository.save(Patient.builder().name("James Wilson").email("james.wilson@email.com").phoneNumber("+1-555-1003").password(pwd).build());

        ana.getPatients().add(john);
        ana.getPatients().add(maria);
        carlos.getPatients().add(james);
        personalRepository.save(ana);
        personalRepository.save(carlos);

        LocalDateTime base = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);

        saveSchedule(ana, base.plusDays(1).withHour(9), base.plusDays(1).withHour(10), ScheduleStatus.AVAILABLE);
        saveSchedule(ana, base.plusDays(1).withHour(10), base.plusDays(1).withHour(11), ScheduleStatus.AVAILABLE);
        Schedule anaBooked = saveSchedule(ana, base.plusDays(2).withHour(9), base.plusDays(2).withHour(10), ScheduleStatus.BOOKED);
        saveSchedule(carlos, base.plusDays(1).withHour(14), base.plusDays(1).withHour(15), ScheduleStatus.AVAILABLE);
        Schedule carlosBooked = saveSchedule(carlos, base.plusDays(3).withHour(10), base.plusDays(3).withHour(11), ScheduleStatus.BOOKED);
        saveSchedule(laura, base.plusDays(2).withHour(15), base.plusDays(2).withHour(16), ScheduleStatus.AVAILABLE);
        saveSchedule(laura, base.plusDays(4).withHour(9), base.plusDays(4).withHour(10), ScheduleStatus.AVAILABLE);

        appointmentRepository.save(Appointment.builder().schedule(anaBooked).patient(john).status(AppointmentStatus.CONFIRMED).build());
        appointmentRepository.save(Appointment.builder().schedule(carlosBooked).patient(maria).status(AppointmentStatus.PENDING).build());
    }

    private Schedule saveSchedule(Personal doctor, LocalDateTime start, LocalDateTime end, ScheduleStatus status) {
        return scheduleRepository.save(Schedule.builder()
                .doctor(doctor).startTime(start).endTime(end).status(status).build());
    }
}
