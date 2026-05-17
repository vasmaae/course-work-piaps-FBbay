package com.hospital.registry.controller;

import com.hospital.registry.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String fullName,
                           Model model,
                           RedirectAttributes ra) {
        log.info("Registration attempt for username='{}'", username);
        try {
            userService.register(username, password, fullName);
            log.info("Registration request submitted for username='{}'", username);
            ra.addFlashAttribute("success", "Заявка отправлена. Дождитесь активации аккаунта администратором.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed for username='{}': {}", username, e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

}
