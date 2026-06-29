package com.multitenant.security;

import com.multitenant.model.core.User;
import com.multitenant.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("userSecurity")
public class UserSecurity {

    private final UserRepository userRepository;

    public UserSecurity(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean canAccessUser(UserDetailsImpl principal, long targetUserId) {
        if ("ROLE_SUPER_ADMIN".equals(principal.getRole())) {
            return true;
        }

        Optional<User> targetUser = userRepository.findById(targetUserId);
        if (targetUser.isEmpty()) {
            return false; // Let controller handle 404
        }

        return targetUser.get().getTenantId().equals(principal.getTenantId());
    }
}
