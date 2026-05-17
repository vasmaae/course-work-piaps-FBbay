package com.hospital.registry.controller;

import com.hospital.registry.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public String list(Model model) {
        log.debug("Admin: loading user list");
        model.addAttribute("users", userService.getAll());
        return "admin/users";
    }

    @PostMapping("/{id}/enable")
    public String enable(@PathVariable Long id, RedirectAttributes ra) {
        log.info("Admin: activating user id={}", id);
        userService.setEnabled(id, true);
        log.info("Admin: user id={} activated", id);
        ra.addFlashAttribute("success", "Пользователь активирован.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/disable")
    public String disable(@PathVariable Long id, RedirectAttributes ra) {
        log.info("Admin: disabling user id={}", id);
        try {
            userService.setEnabled(id, false);
            log.info("Admin: user id={} disabled", id);
            ra.addFlashAttribute("success", "Пользователь заблокирован.");
        } catch (IllegalStateException e) {
            log.warn("Admin: cannot disable user id={}: {}", id, e.getMessage());
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        log.info("Admin: deleting user id={}", id);
        try {
            userService.delete(id);
            log.info("Admin: user id={} deleted", id);
            ra.addFlashAttribute("success", "Пользователь удалён.");
        } catch (IllegalStateException e) {
            log.warn("Admin: cannot delete user id={}: {}", id, e.getMessage());
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
