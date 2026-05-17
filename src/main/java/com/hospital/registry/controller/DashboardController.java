package com.hospital.registry.controller;

import com.hospital.registry.model.AppUser;
import com.hospital.registry.repository.AuditLogRepository;
import com.hospital.registry.repository.MedicalRecordRepository;
import com.hospital.registry.repository.PatientRepository;
import com.hospital.registry.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final PatientRepository patientRepository;
    private final MedicalRecordRepository recordRepository;
    private final AppUserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    @GetMapping("/")
    public String root(Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return isAdmin ? "redirect:/admin/dashboard" : "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String registrarDashboard(Model model) {
        log.debug("Loading registrar dashboard");
        model.addAttribute("totalPatients", patientRepository.countByArchivedFalse());
        model.addAttribute("todayRecords", recordRepository.countByVisitDateAndArchivedFalse(LocalDate.now()));
        model.addAttribute("recentPatients", patientRepository.findTop5ByArchivedFalseOrderByIdDesc());
        return "dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        log.debug("Loading admin dashboard");
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("activeAdmins", userRepository.countByRoleAndEnabled(AppUser.Role.ADMIN, true));
        model.addAttribute("activeRegistrars", userRepository.countByRoleAndEnabled(AppUser.Role.REGISTRAR, true));
        model.addAttribute("pendingUsers", userRepository.countByRoleAndEnabled(AppUser.Role.REGISTRAR, false)
                + userRepository.countByRoleAndEnabled(AppUser.Role.ADMIN, false));
        model.addAttribute("recentLogs", auditLogRepository.findAllByOrderByCreatedAtDesc(
                PageRequest.of(0, 5, Sort.by("createdAt").descending())));
        return "admin/dashboard";
    }
}
