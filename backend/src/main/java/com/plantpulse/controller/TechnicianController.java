package com.plantpulse.controller;

import com.plantpulse.domain.Technician;
import com.plantpulse.repository.TechnicianRepository;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/technicians")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Technicians", description = "ADMIN only — every endpoint in this controller requires the ADMIN role.")
@ApiResponse(responseCode = "403", description = "Caller is not ADMIN")
public class TechnicianController {

    private final TechnicianRepository technicianRepository;

    @GetMapping
    public List<Technician> findAll() {
        return technicianRepository.findAll();
    }

    @PostMapping
    public Technician create(@Valid @RequestBody Technician technician) {
        technician.setId(null);
        return technicianRepository.save(technician);
    }
}
