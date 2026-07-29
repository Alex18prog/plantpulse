package com.plantpulse.controller;

import com.plantpulse.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public, unauthenticated liveness check — meant for hosting platforms
 * (e.g. Render) that poll this to decide whether the instance is up and to
 * keep a free-tier instance from spinning down.
 */
@RestController
@Tag(name = "Health")
public class HealthController {

    @GetMapping("/api/health")
    @SecurityRequirements
    @Operation(summary = "Liveness check", description = "Always returns 200 with no auth required, purely to confirm the process is up.")
    public HealthResponse health() {
        return new HealthResponse("UP");
    }
}
