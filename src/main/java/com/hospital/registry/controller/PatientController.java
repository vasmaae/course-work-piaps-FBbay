package com.hospital.registry.controller;

import com.hospital.registry.model.Patient;
import com.hospital.registry.repository.TherapeuticSectionRepository;
import com.hospital.registry.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final TherapeuticSectionRepository sectionRepository;

    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "") String q,
                       @RequestParam(required = false) Patient.PrivilegeCategory privilege,
                       @RequestParam(required = false) Patient.InsuranceType insuranceType,
                       @RequestParam(required = false) Long sectionId,
                       @RequestParam(required = false) Patient.Gender gender,
                       @RequestParam(defaultValue = "0") int page) {
        var pageable = PageRequest.of(page, 20, Sort.by("lastName").ascending());
        model.addAttribute("patients", patientService.search(q, privilege, insuranceType, sectionId, gender, pageable));
        model.addAttribute("q", q);
        model.addAttribute("privilege", privilege);
        model.addAttribute("insuranceType", insuranceType);
        model.addAttribute("sectionId", sectionId);
        model.addAttribute("gender", gender);
        model.addAttribute("privileges", Patient.PrivilegeCategory.values());
        model.addAttribute("insuranceTypes", Patient.InsuranceType.values());
        model.addAttribute("genders", Patient.Gender.values());
        model.addAttribute("sections", sectionRepository.findAllByOrderByNumber());
        return "patients/list";
    }

    @GetMapping("/archive")
    public String archive(Model model, @RequestParam(defaultValue = "0") int page) {
        var pageable = PageRequest.of(page, 20, Sort.by("updatedAt").descending());
        model.addAttribute("patients", patientService.getArchive(pageable));
        return "patients/archive";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("patient", new Patient());
        model.addAttribute("sections", sectionRepository.findAllByOrderByNumber());
        model.addAttribute("genders", Patient.Gender.values());
        model.addAttribute("privileges", Patient.PrivilegeCategory.values());
        model.addAttribute("insuranceTypes", Patient.InsuranceType.values());
        return "patients/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute Patient patient,
                         BindingResult result,
                         Model model,
                         RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("sections", sectionRepository.findAllByOrderByNumber());
            model.addAttribute("genders", Patient.Gender.values());
            model.addAttribute("privileges", Patient.PrivilegeCategory.values());
            model.addAttribute("insuranceTypes", Patient.InsuranceType.values());
            return "patients/form";
        }
        Patient saved = patientService.create(patient);
        ra.addFlashAttribute("success", "Пациент зарегистрирован. УИН: " + saved.getUin());
        return "redirect:/patients/" + saved.getId();
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("patient", patientService.getById(id));
        model.addAttribute("sections", sectionRepository.findAllByOrderByNumber());
        return "patients/view";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("patient", patientService.getById(id));
        model.addAttribute("sections", sectionRepository.findAllByOrderByNumber());
        model.addAttribute("genders", Patient.Gender.values());
        model.addAttribute("privileges", Patient.PrivilegeCategory.values());
        model.addAttribute("insuranceTypes", Patient.InsuranceType.values());
        return "patients/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute Patient patient,
                         BindingResult result,
                         Model model,
                         RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("sections", sectionRepository.findAllByOrderByNumber());
            model.addAttribute("genders", Patient.Gender.values());
            model.addAttribute("privileges", Patient.PrivilegeCategory.values());
            model.addAttribute("insuranceTypes", Patient.InsuranceType.values());
            return "patients/form";
        }
        patientService.update(id, patient);
        ra.addFlashAttribute("success", "Данные пациента обновлены.");
        return "redirect:/patients/" + id;
    }

    @PostMapping("/{id}/attach-section")
    public String attachSection(@PathVariable Long id,
                                @RequestParam Long sectionId,
                                RedirectAttributes ra) {
        patientService.attachSection(id, sectionId);
        ra.addFlashAttribute("success", "Участок прикреплён.");
        return "redirect:/patients/" + id;
    }

    @PostMapping("/{id}/detach-section")
    public String detachSection(@PathVariable Long id, RedirectAttributes ra) {
        patientService.detachSection(id);
        ra.addFlashAttribute("success", "Участок откреплён.");
        return "redirect:/patients/" + id;
    }

    @PostMapping("/{id}/archive")
    public String archive(@PathVariable Long id, RedirectAttributes ra) {
        patientService.archive(id);
        ra.addFlashAttribute("success", "Пациент перемещён в архив.");
        return "redirect:/patients";
    }

    @PostMapping("/{id}/restore")
    public String restore(@PathVariable Long id, RedirectAttributes ra) {
        patientService.restore(id);
        ra.addFlashAttribute("success", "Пациент восстановлен.");
        return "redirect:/patients/" + id;
    }
}
