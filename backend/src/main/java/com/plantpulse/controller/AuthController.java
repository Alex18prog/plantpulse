package com.plantpulse.controller;

import com.plantpulse.dto.LoginRequest;
import com.plantpulse.dto.LoginResponse;
import com.plantpulse.security.AppUserPrincipal;
import com.plantpulse.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    @SecurityRequirements // overrides the global bearerAuth requirement — this is the one endpoint that doesn't need a token
    @Operation(
            summary = "Exchange email/password for a JWT",
            description = "The only unauthenticated endpoint in the API. Returns a single access token "
                    + "(no refresh rotation) valid for 8h; send it back as `Authorization: Bearer <token>`."
    )
    @ApiResponse(responseCode = "200", description = "Login succeeded")
    @ApiResponse(responseCode = "401", description = "Unknown email or wrong password")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(principal.getUser());

        return new LoginResponse(token, principal.getUser().getEmail(), principal.getUser().getRole());
    }
}
