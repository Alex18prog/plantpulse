package com.plantpulse.controller;

import com.plantpulse.domain.Machine;
import com.plantpulse.dto.TelemetryMessage;
import com.plantpulse.exception.ResourceNotFoundException;
import com.plantpulse.repository.MachineRepository;
import com.plantpulse.repository.TelemetryReadingRepository;
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
    public Machine create(@Valid @RequestBody Machine machine) {
        machine.setId(null);
        return machineRepository.save(machine);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Machine update(@PathVariable Long id, @Valid @RequestBody Machine payload) {
        Machine existing = machineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found: " + id));
        payload.setId(existing.getId());
        return machineRepository.save(payload);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!machineRepository.existsById(id)) {
            throw new ResourceNotFoundException("Machine not found: " + id);
        }
        machineRepository.deleteById(id);
    }
}
