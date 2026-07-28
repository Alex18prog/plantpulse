package com.plantpulse.controller;

import com.plantpulse.domain.Alert;
import com.plantpulse.exception.ResourceNotFoundException;
import com.plantpulse.repository.AlertRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts")
public class AlertController {

    private final AlertRepository alertRepository;

    @GetMapping
    @Operation(summary = "List unresolved alerts, newest first", description = "\"Active\" means not yet resolved — resolved alerts aren't returned here.")
    public List<Alert> findActive() {
        return alertRepository.findByResolvedFalseOrderByCreatedAtDesc();
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark an alert resolved (ADMIN only)")
    @ApiResponse(responseCode = "403", description = "Caller is not ADMIN")
    public Alert resolve(@PathVariable Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found: " + id));
        alert.setResolved(true);
        return alertRepository.save(alert);
    }
}
