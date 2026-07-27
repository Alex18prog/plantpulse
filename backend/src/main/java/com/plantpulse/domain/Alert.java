package com.plantpulse.domain;

import com.plantpulse.domain.enums.AlertSeverity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    @NotBlank
    @Column(nullable = false)
    private String message;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private boolean resolved = false;

    @OneToOne
    @JoinColumn(name = "work_order_id")
    private WorkOrder linkedWorkOrder;
}
