package com.plantpulse.dto;

import com.plantpulse.domain.enums.Role;

public record LoginResponse(
        String token,
        String email,
        Role role
) {
}
