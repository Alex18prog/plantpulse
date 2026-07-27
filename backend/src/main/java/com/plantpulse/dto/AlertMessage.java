package com.plantpulse.dto;

import com.plantpulse.domain.enums.AlertSeverity;

import java.time.Instant;

public record AlertMessage(
        Long alertId,
        Long machineId,
        String machineName,
        AlertSeverity severity,
        String message,
        Long linkedWorkOrderId,
        Instant timestamp
) {
}
