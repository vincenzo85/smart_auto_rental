package com.smartautorental.platform.identity.service;

import com.smartautorental.platform.common.exception.BusinessException;
import com.smartautorental.platform.common.exception.ErrorCode;
import com.smartautorental.platform.identity.dto.AuthResponse;
import com.smartautorental.platform.identity.dto.LoginRequest;
import com.smartautorental.platform.identity.dto.RegisterRequest;
import com.smartautorental.platform.identity.dto.UserResponse;
import com.smartautorental.platform.identity.model.User;
import com.smartautorental.platform.identity.model.UserRole;
import com.smartautorental.platform.identity.repo.UserRepository;
import com.smartautorental.platform.security.JwtService;
import com.smartautorental.platform.security.UserPrincipal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException(ErrorCode.CONFLICT, "Email already registered");
        }

        User user = new User();
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);

        User saved = userRepository.save(user);
        return toAuthResponse(UserPrincipal.from(saved));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "User not found"));

        return toAuthResponse(UserPrincipal.from(user));
    }

    private AuthResponse toAuthResponse(UserPrincipal principal) {
        String token = jwtService.generateToken(principal);
        Instant expiresAt = Instant.now().plusSeconds(jwtService.getExpirationMinutes() * 60);
        return new AuthResponse(
                token,
                expiresAt,
                new UserResponse(principal.id(), principal.email(), UserRole.valueOf(principal.role()))
        );
    }
}
