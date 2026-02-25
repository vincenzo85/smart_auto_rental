package com.smartautorental.platform.security;

import com.smartautorental.platform.common.exception.BusinessException;
import com.smartautorental.platform.common.exception.ErrorCode;
import com.smartautorental.platform.identity.model.User;
import com.smartautorental.platform.identity.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User requireCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        return userRepository.findById(principal.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "User not found"));
    }

    public String currentActorEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal.email();
        }
        return "system";
    }
}
