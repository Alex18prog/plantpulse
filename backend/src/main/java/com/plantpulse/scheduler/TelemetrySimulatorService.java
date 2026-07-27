package com.plantpulse.scheduler;

import com.plantpulse.domain.Alert;
import com.plantpulse.domain.Machine;
import com.plantpulse.domain.enums.AlertSeverity;
import com.plantpulse.domain.enums.MachineStatus;
import com.plantpulse.dto.AlertMessage;
import com.plantpulse.dto.TelemetryMessage;
import com.plantpulse.repository.AlertRepository;
import com.plantpulse.repository.MachineRepository;
import com.plantpulse.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Stands in for real plant-floor sensors (temperature + vibration probes).
 * Every tick it perturbs each machine's baseline reading with noise and an
 * occasional "drift" event, broadcasts it live over WebSocket, and — if a
 * threshold is breached — opens an Alert plus an auto-generated corrective
 * WorkOrder, exactly like a real condition-monitoring system would.
 *
 * This is what makes PlantPulse feel alive in a demo: no manual data entry
 * required, the dashboard just reacts.
 */
@Component
@RequiredArgsConstructor
public class TelemetrySimulatorService {

    private final MachineRepository machineRepository;
    private final AlertRepository alertRepository;
    private final WorkOrderService workOrderService;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${plantpulse.thresholds.temperature-warning}")
    private double tempWarning;

    @Value("${plantpulse.thresholds.temperature-critical}")
    private double tempCritical;

    @Value("${plantpulse.thresholds.vibration-warning}")
    private double vibWarning;

    @Value("${plantpulse.thresholds.vibration-critical}")
    private double vibCritical;

    @Scheduled(fixedRateString = "${plantpulse.telemetry.interval-ms:3000}")
    public void tick() {
        List<Machine> machines = machineRepository.findAll();

        for (Machine machine : machines) {
            if (machine.getStatus() == MachineStatus.DOWN) {
                continue; // a stopped machine has nothing to read
            }

            double temperature = wobble(machine.getBaselineTemperature(), 4.0);
            double vibration = wobble(machine.getBaselineVibration(), 0.8);
            int rpm = 1400 + ThreadLocalRandom.current().nextInt(-40, 40);

            messagingTemplate.convertAndSend("/topic/telemetry",
                    new TelemetryMessage(machine.getId(), machine.getName(), temperature, vibration, rpm, Instant.now()));

            evaluateThresholds(machine, temperature, vibration);
        }
    }

    private double wobble(double baseline, double maxDelta) {
        double delta = ThreadLocalRandom.current().nextDouble(-maxDelta, maxDelta);
        // 2% chance of a sharper spike, so alerts don't feel purely cosmetic
        if (ThreadLocalRandom.current().nextInt(100) < 2) {
            delta += maxDelta * 3;
        }
        return Math.round((baseline + delta) * 10.0) / 10.0;
    }

    void evaluateThresholds(Machine machine, double temperature, double vibration) {
        if (temperature >= tempCritical || vibration >= vibCritical) {
            raiseAlert(machine, AlertSeverity.CRITICAL,
                    "Critical reading on %s: %.1f°C / %.1fmm/s".formatted(machine.getName(), temperature, vibration),
                    true);
        } else if (temperature >= tempWarning || vibration >= vibWarning) {
            raiseAlert(machine, AlertSeverity.WARNING,
                    "%s approaching limits: %.1f°C / %.1fmm/s".formatted(machine.getName(), temperature, vibration),
                    false);
        }
    }

    private void raiseAlert(Machine machine, AlertSeverity severity, String message, boolean openWorkOrder) {
        Alert alert = Alert.builder()
                .machine(machine)
                .severity(severity)
                .message(message)
                .build();

        if (openWorkOrder) {
            alert.setLinkedWorkOrder(workOrderService.openAutoCorrectiveOrder(machine, message));
        }

        alert = alertRepository.save(alert);

        messagingTemplate.convertAndSend("/topic/alerts", new AlertMessage(
                alert.getId(),
                machine.getId(),
                machine.getName(),
                alert.getSeverity(),
                alert.getMessage(),
                alert.getLinkedWorkOrder() != null ? alert.getLinkedWorkOrder().getId() : null,
                alert.getCreatedAt()
        ));
    }
}
