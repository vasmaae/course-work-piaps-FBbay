package com.hospital.registry.repository;

import com.hospital.registry.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);

    long countByRole(AppUser.Role role);

    long countByRoleAndEnabled(AppUser.Role role, boolean enabled);
}
