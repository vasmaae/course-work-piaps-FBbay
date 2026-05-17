package com.hospital.registry.controller;

import com.hospital.registry.model.Patient;
import com.hospital.registry.repository.TherapeuticSectionRepository;
import com.hospital.registry.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Slf4j
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
        log.debug("Patient list: q='{}', privilege={}, insuranceType={}, sectionId={}, gender={}, page={}",
                q, privilege, insuranceType, sectionId, gender, page);
        var pageable = PageRequest.of(page, 5, Sort.by("lastName").ascending());
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
        log.debug("Patient archive page={}", page);
        var pageable = PageRequest.of(page, 20, Sort.by("updatedAt").descending());
        model.addAttribute("patients", patientService.getArchive(pageable));
        return "patients/archive";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        log.debug("Opening new patient form");
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
            log.warn("Patient creation failed validation: {} errors", result.getErrorCount());
            model.addAttribute("sections", sectionRepository.findAllByOrderByNumber());
            model.addAttribute("genders", Patient.Gender.values());
            model.addAttribute("privileges", Patient.PrivilegeCategory.values());
            model.addAttribute("insuranceTypes", Patient.InsuranceType.values());
            return "patients/form";
        }
        log.info("Creating patient: '{}'", patient.getFullName());
        Patient saved = patientService.create(patient);
        log.info("Patient created: id={}, UIN={}", saved.getId(), saved.getUin());
        ra.addFlashAttribute("success", "Пациент зарегистрирован. УИН: " + saved.getUin());
        return "redirect:/patients/" + saved.getId();
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        log.debug("Viewing patient id={}", id);
        model.addAttribute("patient", patientService.getById(id));
        model.addAttribute("sections", sectionRepository.findAllByOrderByNumber());
        return "patients/view";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        log.debug("Opening edit form for patient id={}", id);
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
            log.warn("Patient update id={} failed validation: {} errors", id, result.getErrorCount());
            model.addAttribute("sections", sectionRepository.findAllByOrderByNumber());
            model.addAttribute("genders", Patient.Gender.values());
            model.addAttribute("privileges", Patient.PrivilegeCategory.values());
            model.addAttribute("insuranceTypes", Patient.InsuranceType.values());
            return "patients/form";
        }
        log.info("Updating patient id={}", id);
        patientService.update(id, patient);
        log.info("Patient id={} updated", id);
        ra.addFlashAttribute("success", "Данные пациента обновлены.");
        return "redirect:/patients/" + id;
    }

    @PostMapping("/{id}/attach-section")
    public String attachSection(@PathVariable Long id,
                                @RequestParam Long sectionId,
                                RedirectAttributes ra) {
        log.info("Attaching section id={} to patient id={}", sectionId, id);
        patientService.attachSection(id, sectionId);
        ra.addFlashAttribute("success", "Участок прикреплён.");
        return "redirect:/patients/" + id;
    }

    @PostMapping("/{id}/detach-section")
    public String detachSection(@PathVariable Long id, RedirectAttributes ra) {
        log.info("Detaching section from patient id={}", id);
        patientService.detachSection(id);
        ra.addFlashAttribute("success", "Участок откреплён.");
        return "redirect:/patients/" + id;
    }

    @PostMapping("/{id}/archive")
    public String archive(@PathVariable Long id, RedirectAttributes ra) {
        log.info("Archiving patient id={}", id);
        patientService.archive(id);
        log.info("Patient id={} archived", id);
        ra.addFlashAttribute("success", "Пациент перемещён в архив.");
        return "redirect:/patients";
    }

    @PostMapping("/{id}/restore")
    public String restore(@PathVariable Long id, RedirectAttributes ra) {
        log.info("Restoring patient id={}", id);
        patientService.restore(id);
        log.info("Patient id={} restored", id);
        ra.addFlashAttribute("success", "Пациент восстановлен.");
        return "redirect:/patients/" + id;
    }

    @GetMapping("/{id}/card")
    public String printCard(@PathVariable Long id, Model model) {
        log.debug("Printing card for patient id={}", id);
        model.addAttribute("patient", patientService.getById(id));
        return "patients/print-card";
    }
}
