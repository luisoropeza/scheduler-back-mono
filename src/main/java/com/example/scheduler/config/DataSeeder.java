package com.example.scheduler.config;

import com.example.scheduler.config.tenant.TenantContext;
import com.example.scheduler.entity.*;
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
    private final ClinicRepository clinicRepository;
    private final SpecialtyRepository specialtyRepository;
    private final RoleRepository roleRepository;
    private final PersonalRepository personalRepository;
    private final PatientRepository patientRepository;
    private final PatientAccountRepository patientAccountRepository;
    private final PersonalAccountRepository personalAccountRepository;
    private final ScheduleRepository scheduleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final int[] SLOT_HOURS = {9, 10, 11, 14, 15};

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        if (specialtyRepository.count() > 0) return;

        Clinic downtown = clinicRepository.save(Clinic.builder().name("Downtown Clinic").phoneNumber("+1-555-2001").build());
        Clinic uptown = clinicRepository.save(Clinic.builder().name("Uptown Clinic").phoneNumber("+1-555-2002").build());

        String pwd = passwordEncoder.encode("password123");

        Role admin = roleRepository.save(Role.builder().name("ADMINISTRATOR").build());
        Role doctor = roleRepository.save(Role.builder().name("DOCTOR").build());
        Role receptionist = roleRepository.save(Role.builder().name("RECEPTIONIST").build());

        try {
            TenantContext.setCurrentTenant(downtown.getId().toString());

            Specialty gm = specialtyRepository.save(Specialty.builder().name("General Medicine").build());
            Specialty dent = specialtyRepository.save(Specialty.builder().name("Dentistry").build());
            Specialty peds = specialtyRepository.save(Specialty.builder().name("Pediatrics").build());

            personalRepository.save(Personal.builder().account(seedPersonalAccount("Downtown Admin", "admin.downtown@clinic.com", pwd, admin)).build());

            Personal ana = personalRepository.save(Personal.builder().account(seedPersonalAccount("Dr. Ana García", "ana.garcia@clinic.com", pwd, doctor)).specialty(gm).build());
            Personal carlos = personalRepository.save(Personal.builder().account(seedPersonalAccount("Dr. Carlos Méndez", "carlos.mendez@clinic.com", pwd, doctor)).specialty(dent).build());
            Personal laura = personalRepository.save(Personal.builder().account(seedPersonalAccount("Dr. Laura Torres", "laura.torres@clinic.com", pwd, doctor)).specialty(peds).build());
            personalRepository.save(Personal.builder().account(seedPersonalAccount("Maria Ramos", "maria.ramos@clinic.com", pwd, receptionist)).build());

            PatientAccount johnAccount = patientAccountRepository.save(PatientAccount.builder().name("John Smith").email("john.smith@email.com").phoneNumber("+1-555-1001").password(pwd).build());
            PatientAccount mariaAccount = patientAccountRepository.save(PatientAccount.builder().name("María López").email("maria.lopez@email.com").phoneNumber("+1-555-1002").password(pwd).build());
            Patient john = patientRepository.save(Patient.builder().account(johnAccount).build());
            Patient maria = patientRepository.save(Patient.builder().account(mariaAccount).build());

            ana.getPatients().add(john);
            ana.getPatients().add(maria);
            personalRepository.save(ana);

            seedSchedules(ana, carlos, laura);
        } finally {
            TenantContext.clear();
        }

        try {
            TenantContext.setCurrentTenant(uptown.getId().toString());

            Specialty cardio = specialtyRepository.save(Specialty.builder().name("Cardiology").build());
            Specialty derma = specialtyRepository.save(Specialty.builder().name("Dermatology").build());
            Specialty trauma = specialtyRepository.save(Specialty.builder().name("Traumatology").build());

            personalRepository.save(Personal.builder().account(seedPersonalAccount("Uptown Admin", "admin.uptown@clinic.com", pwd, admin)).build());

            Personal sofia = personalRepository.save(Personal.builder().account(seedPersonalAccount("Dr. Sofía Ramírez", "sofia.ramirez@clinic.com", pwd, doctor)).specialty(cardio).build());
            Personal diego = personalRepository.save(Personal.builder().account(seedPersonalAccount("Dr. Diego Fernández", "diego.fernandez@clinic.com", pwd, doctor)).specialty(derma).build());
            Personal valentina = personalRepository.save(Personal.builder().account(seedPersonalAccount("Dr. Valentina Cruz", "valentina.cruz@clinic.com", pwd, doctor)).specialty(trauma).build());
            personalRepository.save(Personal.builder().account(seedPersonalAccount("Pedro Álvarez", "pedro.alvarez@clinic.com", pwd, receptionist)).build());

            PatientAccount jamesAccount = patientAccountRepository.save(PatientAccount.builder().name("James Wilson").email("james.wilson@email.com").phoneNumber("+1-555-1003").password(pwd).build());
            Patient james = patientRepository.save(Patient.builder().account(jamesAccount).build());

            sofia.getPatients().add(james);
            personalRepository.save(sofia);

            seedSchedules(sofia, diego, valentina);
        } finally {
            TenantContext.clear();
        }
    }

    private PersonalAccount seedPersonalAccount(String name, String email, String encodedPassword, Role role) {

        return personalAccountRepository.save(PersonalAccount.builder().name(name).email(email).password(encodedPassword).role(role).build());
    }

    private void seedSchedules(Personal... doctors) {
        LocalDateTime base = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        for (Personal d : doctors) {
            for (int day = 1; day <= 2; day++) {
                for (int hour : SLOT_HOURS) {
                    LocalDateTime start = base.plusDays(day).withHour(hour);
                    saveSchedule(d, start, start.plusHours(1));
                }
            }
        }
    }

    private void saveSchedule(Personal doctor, LocalDateTime start, LocalDateTime end) {
        scheduleRepository.save(Schedule.builder()
                .doctor(doctor).startTime(start).endTime(end).status(ScheduleStatus.AVAILABLE).build());
    }
}
