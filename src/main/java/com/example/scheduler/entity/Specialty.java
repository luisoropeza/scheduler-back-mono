package com.example.scheduler.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.TenantId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "specialties", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "clinic_id"}))
public class Specialty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @TenantId
    @Column(name = "clinic_id", nullable = false, updatable = false)
    private String clinicId;
}
