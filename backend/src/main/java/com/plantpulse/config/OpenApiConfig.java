package com.plantpulse.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI (/swagger-ui.html) and the raw spec (/v3/api-docs) require a
 * JWT like every other endpoint — see SecurityConfig's anyRequest().authenticated()
 * catch-all, which applies to these paths too since nothing here or there
 * permits them. Get a token via POST /api/auth/login, then paste it into
 * Swagger UI's "Authorize" button (bearerAuth below) to try endpoints out.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "PlantPulse API",
                version = "v1",
                description = "CMMS + real-time IoT condition monitoring REST API. "
                        + "WebSocket topics (/topic/telemetry, /topic/alerts) are not part of this spec — "
                        + "see the README for the STOMP/SockJS contract."
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
