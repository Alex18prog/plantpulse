package com.plantpulse.domain;

import com.plantpulse.domain.enums.MachineStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "machines")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MachineStatus status;

    private LocalDate installDate;

    // Baseline operating values used by the telemetry simulator
    @Builder.Default
    private Double baselineTemperature = 55.0;

    @Builder.Default
    private Double baselineVibration = 2.5;
}
