package com.hospital.registry.controller;

import com.hospital.registry.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("/admin/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        log.debug("Admin: audit log page={}", page);
        var pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        model.addAttribute("logs", auditLogRepository.findAllByOrderByCreatedAtDesc(pageable));
        return "admin/audit";
    }
}
