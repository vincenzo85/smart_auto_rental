package com.smartautorental.platform.identity.dto;

import java.time.Instant;

public record AuthResponse(
        String token,
        Instant expiresAt,
        UserResponse user
) {
}
