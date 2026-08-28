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
@Table(name = "patients", uniqueConstraints = @UniqueConstraint(columnNames = {"patient_account_id", "clinic_id"}))
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_account_id")
    private Account account;

    @TenantId
    @Column(name = "clinic_id", nullable = false, updatable = false)
    private String clinicId;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @ManyToMany(mappedBy = "patients")
    @Builder.Default
    private List<Personal> doctors = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id")
    private Role role;
}
