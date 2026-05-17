package com.hospital.registry.controller;

import com.hospital.registry.model.Doctor;
import com.hospital.registry.service.DoctorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public String list(Model model) {
        log.debug("Loading doctor list");
        model.addAttribute("doctors", doctorService.getAll());
        return "doctors/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        log.debug("Opening new doctor form");
        model.addAttribute("doctor", new Doctor());
        return "doctors/form";
    }

    @PostMapping
    public String create(@ModelAttribute Doctor doctor, RedirectAttributes ra) {
        log.info("Creating doctor: '{}'", doctor.getFullName());
        doctorService.save(doctor);
        log.info("Doctor created: '{}'", doctor.getFullName());
        ra.addFlashAttribute("success", "Врач добавлен.");
        return "redirect:/doctors";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        log.debug("Opening edit form for doctor id={}", id);
        model.addAttribute("doctor", doctorService.getById(id));
        return "doctors/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Doctor doctor, RedirectAttributes ra) {
        log.info("Updating doctor id={}", id);
        doctor.setId(id);
        doctorService.save(doctor);
        log.info("Doctor id={} updated", id);
        ra.addFlashAttribute("success", "Данные врача обновлены.");
        return "redirect:/doctors";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        log.info("Deleting doctor id={}", id);
        doctorService.delete(id);
        log.info("Doctor id={} deleted", id);
        ra.addFlashAttribute("success", "Врач удалён.");
        return "redirect:/doctors";
    }
}
