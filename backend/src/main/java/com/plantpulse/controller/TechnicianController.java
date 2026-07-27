package com.plantpulse.controller;

import com.plantpulse.domain.Technician;
import com.plantpulse.repository.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/technicians")
@RequiredArgsConstructor
public class TechnicianController {

    private final TechnicianRepository technicianRepository;

    @GetMapping
    public List<Technician> findAll() {
        return technicianRepository.findAll();
    }

    @PostMapping
    public Technician create(@RequestBody Technician technician) {
        technician.setId(null);
        return technicianRepository.save(technician);
    }
}
