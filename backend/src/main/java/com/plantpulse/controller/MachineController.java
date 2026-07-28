package com.plantpulse.controller;

import com.plantpulse.domain.Machine;
import com.plantpulse.dto.TelemetryMessage;
import com.plantpulse.exception.ResourceNotFoundException;
import com.plantpulse.repository.MachineRepository;
import com.plantpulse.repository.TelemetryReadingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/machines")
@RequiredArgsConstructor
@Tag(name = "Machines")
public class MachineController {

    private final MachineRepository machineRepository;
    private final TelemetryReadingRepository telemetryReadingRepository;

    @GetMapping
    public List<Machine> findAll() {
        return machineRepository.findAll();
    }

    @GetMapping("/{id}")
    public Machine findById(@PathVariable Long id) {
        return machineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found: " + id));
    }

    @GetMapping("/{id}/telemetry-history")
    @Operation(
            summary = "Last 24h of sampled telemetry for a machine",
            description = "Not every simulator tick is kept — see TelemetrySimulatorService's "
                    + "sample-every-n-ticks config (~1 point every 30s by default) — and readings older "
                    + "than the retention window are purged, so this is a trend view, not a full time-series export."
    )
    public List<TelemetryMessage> telemetryHistory(@PathVariable Long id) {
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found: " + id));

        Instant since = Instant.now().minus(24, ChronoUnit.HOURS);
        return telemetryReadingRepository.findByMachineIdAndRecordedAtAfterOrderByRecordedAtAsc(id, since).stream()
                .map(r -> new TelemetryMessage(machine.getId(), machine.getName(), r.getTemperature(), r.getVibration(), r.getRpm(), r.getRecordedAt()))
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a machine (ADMIN only)")
    @ApiResponse(responseCode = "403", description = "Caller is not ADMIN")
    public Machine create(@Valid @RequestBody Machine machine) {
        machine.setId(null);
        return machineRepository.save(machine);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Replace a machine (ADMIN only)",
            description = "Full replace, not a partial update — send the complete object back, "
                    + "including fields like maintenanceIntervalDays you don't intend to change."
    )
    @ApiResponse(responseCode = "403", description = "Caller is not ADMIN")
    public Machine update(@PathVariable Long id, @Valid @RequestBody Machine payload) {
        Machine existing = machineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found: " + id));
        payload.setId(existing.getId());
        return machineRepository.save(payload);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a machine (ADMIN only)")
    @ApiResponse(responseCode = "403", description = "Caller is not ADMIN")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!machineRepository.existsById(id)) {
            throw new ResourceNotFoundException("Machine not found: " + id);
        }
        machineRepository.deleteById(id);
    }
}
