package com.hospital.registry.service;

import com.hospital.registry.model.AppUser;
import com.hospital.registry.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(String username, String password, String fullName) {
        log.info("Registering new user: username='{}'", username);
        if (userRepository.existsByUsername(username)) {
            log.warn("Registration rejected: username='{}' already exists", username);
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRole(AppUser.Role.REGISTRAR);
        user.setEnabled(false);
        userRepository.save(user);
        log.info("User registered (pending activation): username='{}'", username);
    }

    @Transactional(readOnly = true)
    public List<AppUser> getAll() {
        log.debug("Fetching all users");
        return userRepository.findAll();
    }

    @Transactional
    public void setEnabled(Long id, boolean enabled) {
        log.info("Setting enabled={} for user id={}", enabled, id);
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + id));
        if (!enabled && user.isEnabled()) {
            checkLastActiveRole(user.getRole());
        }
        user.setEnabled(enabled);
        userRepository.save(user);
        log.info("User id={} (username='{}') enabled={}", id, user.getUsername(), enabled);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting user id={}", id);
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + id));
        checkLastRoleTotal(user.getRole());
        userRepository.deleteById(id);
        log.info("User id={} (username='{}') deleted", id, user.getUsername());
    }

    private void checkLastActiveRole(AppUser.Role role) {
        long activeCount = userRepository.countByRoleAndEnabled(role, true);
        log.debug("Active users with role {}: {}", role, activeCount);
        if (activeCount <= 1) {
            String roleName = role == AppUser.Role.ADMIN ? "администратор" : "регистратор";
            log.warn("Blocked disabling last active {} (id check)", roleName);
            throw new IllegalStateException(
                    "Невозможно заблокировать: в системе должен оставаться хотя бы один активный " + roleName + ".");
        }
    }

    private void checkLastRoleTotal(AppUser.Role role) {
        long totalCount = userRepository.countByRole(role);
        log.debug("Total users with role {}: {}", role, totalCount);
        if (totalCount <= 1) {
            String roleName = role == AppUser.Role.ADMIN ? "администратор" : "регистратор";
            log.warn("Blocked deleting last {} (id check)", roleName);
            throw new IllegalStateException(
                    "Невозможно удалить: в системе должен оставаться хотя бы один " + roleName + ".");
        }
    }
}
