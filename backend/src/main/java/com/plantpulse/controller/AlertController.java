package com.plantpulse.controller;

import com.plantpulse.domain.Alert;
import com.plantpulse.exception.ResourceNotFoundException;
import com.plantpulse.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertRepository alertRepository;

    @GetMapping
    public List<Alert> findActive() {
        return alertRepository.findByResolvedFalseOrderByCreatedAtDesc();
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public Alert resolve(@PathVariable Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found: " + id));
        alert.setResolved(true);
        return alertRepository.save(alert);
    }
}
