package com.plantpulse.controller;

import com.plantpulse.domain.Machine;
import com.plantpulse.repository.MachineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/machines")
@RequiredArgsConstructor
public class MachineController {

    private final MachineRepository machineRepository;

    @GetMapping
    public List<Machine> findAll() {
        return machineRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Machine> findById(@PathVariable Long id) {
        return machineRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Machine create(@RequestBody Machine machine) {
        machine.setId(null);
        return machineRepository.save(machine);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Machine> update(@PathVariable Long id, @RequestBody Machine payload) {
        return machineRepository.findById(id)
                .map(existing -> {
                    payload.setId(existing.getId());
                    return ResponseEntity.ok(machineRepository.save(payload));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!machineRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        machineRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
