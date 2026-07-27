package com.plantpulse.domain;

import com.plantpulse.domain.enums.TechnicianStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "technicians")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Technician {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String specialty;

    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TechnicianStatus status;
}
