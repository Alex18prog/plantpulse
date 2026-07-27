package com.plantpulse.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * A downsampled telemetry point kept for trend charts (see machine detail
 * page). Not every simulator tick is persisted — see
 * TelemetrySimulatorService's sample-every-n-ticks config — and rows older
 * than the retention window are purged on a schedule, so this never needs
 * to be a proper time-series store for a demo of this size.
 */
@Entity
@Table(name = "telemetry_readings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;

    @NotNull
    @Column(nullable = false)
    private Double temperature;

    @NotNull
    @Column(nullable = false)
    private Double vibration;

    @NotNull
    @Column(nullable = false)
    private Integer rpm;

    @NotNull
    @Column(nullable = false)
    private Instant recordedAt;
}
