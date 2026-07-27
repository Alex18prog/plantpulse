package com.plantpulse.dto;

import java.time.Instant;

/**
 * Payload broadcast over WebSocket (/topic/telemetry) every simulation tick,
 * and also the shape returned by GET /api/machines/{id}/telemetry-history —
 * a sampled subset of these ticks is persisted as TelemetryReading and
 * mapped back to this same shape, so the frontend can reuse one type for
 * both the live feed and historical charts.
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
