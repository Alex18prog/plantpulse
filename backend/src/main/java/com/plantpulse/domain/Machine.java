package com.plantpulse.domain;

import com.plantpulse.domain.enums.MachineStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String type;

    private String location;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MachineStatus status;

    private LocalDate installDate;

    // Baseline operating values used by the telemetry simulator
    @NotNull
    @Builder.Default
    private Double baselineTemperature = 55.0;

    @NotNull
    @Builder.Default
    private Double baselineVibration = 2.5;

    /** Calendar days between scheduled preventive maintenance visits. Null disables scheduling for this machine. */
    @Min(1)
    private Integer maintenanceIntervalDays;
}
