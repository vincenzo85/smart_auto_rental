package com.smartautorental.platform.bootstrap;

import com.smartautorental.platform.identity.model.User;
import com.smartautorental.platform.identity.model.UserRole;
import com.smartautorental.platform.identity.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public ApplicationRunner seedUsers() {
        return args -> {
            createUserIfMissing("admin@smartauto.local", "Admin123!", UserRole.ADMIN);
            createUserIfMissing("operator@smartauto.local", "Operator123!", UserRole.OPERATOR);
            createUserIfMissing("customer@smartauto.local", "Customer123!", UserRole.CUSTOMER);
        };
    }

    private void createUserIfMissing(String email, String password, UserRole role) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setActive(true);
        userRepository.save(user);
    }
}
