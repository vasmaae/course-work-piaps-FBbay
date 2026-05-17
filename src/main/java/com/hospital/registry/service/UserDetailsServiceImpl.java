package com.hospital.registry.service;

import com.hospital.registry.model.AppUser;
import com.hospital.registry.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AppUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username='{}'", username);
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Authentication failed: user '{}' not found", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });
        if (!user.isEnabled()) {
            log.warn("Authentication failed: user '{}' is disabled (pending activation)", username);
            throw new UsernameNotFoundException("Аккаунт ожидает активации администратором");
        }
        log.debug("User '{}' loaded successfully, role={}", username, user.getRole());
        return new User(
                user.getUsername(),
                user.getPasswordHash(),
                true, true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
