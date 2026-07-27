package com.plantpulse.dto;

import java.time.Instant;

/**
 * Payload broadcast over WebSocket (/topic/telemetry) every simulation tick.
 * Not persisted — this is a live signal, not historical record.
 */
public record TelemetryMessage(
        Long machineId,
        String machineName,
        double temperature,
        double vibration,
        int rpm,
        Instant timestamp
) {
}
