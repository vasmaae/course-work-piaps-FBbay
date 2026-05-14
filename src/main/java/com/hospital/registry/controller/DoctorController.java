package com.hospital.registry.controller;

import com.hospital.registry.model.Doctor;
import com.hospital.registry.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("doctors", doctorService.getAll());
        return "doctors/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("doctor", new Doctor());
        return "doctors/form";
    }

    @PostMapping
    public String create(@ModelAttribute Doctor doctor, RedirectAttributes ra) {
        doctorService.save(doctor);
        ra.addFlashAttribute("success", "Врач добавлен.");
        return "redirect:/doctors";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("doctor", doctorService.getById(id));
        return "doctors/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Doctor doctor, RedirectAttributes ra) {
        doctor.setId(id);
        doctorService.save(doctor);
        ra.addFlashAttribute("success", "Данные врача обновлены.");
        return "redirect:/doctors";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        doctorService.delete(id);
        ra.addFlashAttribute("success", "Врач удалён.");
        return "redirect:/doctors";
    }
}
