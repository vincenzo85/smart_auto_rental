package com.smartautorental.platform.identity.dto;

import com.smartautorental.platform.identity.model.UserRole;

public record UserResponse(
        Long id,
        String email,
        UserRole role
) {
}
