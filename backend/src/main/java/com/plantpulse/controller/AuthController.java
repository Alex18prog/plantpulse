package com.plantpulse.controller;

import com.plantpulse.dto.LoginRequest;
import com.plantpulse.dto.LoginResponse;
import com.plantpulse.security.AppUserPrincipal;
import com.plantpulse.security.JwtService;
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
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(principal.getUser());

        return new LoginResponse(token, principal.getUser().getEmail(), principal.getUser().getRole());
    }
}
