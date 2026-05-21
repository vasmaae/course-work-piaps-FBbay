package com.hospital.registry.controller;

import com.hospital.registry.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Slf4j
@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/workload")
    public String workload(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Model model) {

        if (from == null) from = LocalDate.now().withDayOfMonth(1);
        if (to == null) to = LocalDate.now();

        if (from.isAfter(to)) {
            LocalDate tmp = from; from = to; to = tmp;
            model.addAttribute("dateWarning", "Начальная дата позже конечной — даты поменяны местами.");
        }

        log.info("Report: workload from={} to={}", from, to);
        model.addAttribute("workload", reportService.getWorkloadReport(from, to));
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "reports/workload";
    }

    @GetMapping("/vouchers")
    public String vouchers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Model model) {

        if (from == null) from = LocalDate.now().withDayOfMonth(1);
        if (to == null) to = LocalDate.now();

        if (from.isAfter(to)) {
            LocalDate tmp = from; from = to; to = tmp;
            model.addAttribute("dateWarning", "Начальная дата позже конечной — даты поменяны местами.");
        }

        log.info("Report: vouchers from={} to={}", from, to);
        model.addAttribute("records", reportService.getRecordsByPeriod(from, to));
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "reports/vouchers";
    }

    @GetMapping("/diagnoses")
    public String diagnoses(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Model model) {

        if (from == null) from = LocalDate.now().minusYears(1);
        if (to == null) to = LocalDate.now();

        if (from.isAfter(to)) {
            LocalDate tmp = from; from = to; to = tmp;
            model.addAttribute("dateWarning", "Начальная дата позже конечной — даты поменяны местами.");
        }

        log.info("Report: diagnoses from={} to={}", from, to);
        model.addAttribute("stats", reportService.getDiagnosisStats(from, to));
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "reports/diagnoses";
    }

    @GetMapping("/privileges")
    public String privileges(Model model) {
        log.info("Report: privileges");
        model.addAttribute("stats", reportService.getPrivilegeStats());
        model.addAttribute("total", reportService.getPrivilegeStats().stream()
                .mapToLong(row -> (Long) row[1]).sum());
        return "reports/privileges";
    }
}
