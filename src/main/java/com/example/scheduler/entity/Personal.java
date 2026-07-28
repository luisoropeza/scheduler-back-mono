package com.example.scheduler.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.TenantId;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "personal")
public class Personal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "personal_account_id")
    private PersonalAccount account;

    @ManyToOne
    @JoinColumn(name = "specialty_id")
    private Specialty specialty;

    @TenantId
    @Column(name = "clinic_id", nullable = false, updatable = false)
    private String clinicId;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @ManyToMany
    @JoinTable(
        name = "doctor_patient",
        joinColumns = @JoinColumn(name = "doctor_id"),
        inverseJoinColumns = @JoinColumn(name = "patient_id")
    )
    @Builder.Default
    private List<Patient> patients = new ArrayList<>();
}
