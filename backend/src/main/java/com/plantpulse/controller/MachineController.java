package com.plantpulse.controller;

import com.plantpulse.domain.Machine;
import com.plantpulse.exception.ResourceNotFoundException;
import com.plantpulse.repository.MachineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public Machine findById(@PathVariable Long id) {
        return machineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found: " + id));
    }

    @PostMapping
    public Machine create(@RequestBody Machine machine) {
        machine.setId(null);
        return machineRepository.save(machine);
    }

    @PutMapping("/{id}")
    public Machine update(@PathVariable Long id, @RequestBody Machine payload) {
        Machine existing = machineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found: " + id));
        payload.setId(existing.getId());
        return machineRepository.save(payload);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!machineRepository.existsById(id)) {
            throw new ResourceNotFoundException("Machine not found: " + id);
        }
        machineRepository.deleteById(id);
    }
}
