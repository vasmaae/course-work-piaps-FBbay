package com.hospital.registry.controller;

import com.hospital.registry.model.MedicalRecord;
import com.hospital.registry.service.DoctorService;
import com.hospital.registry.service.MedicalRecordService;
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
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService recordService;
    private final PatientService patientService;
    private final DoctorService doctorService;

    @GetMapping("/patients/{patientId}/records")
    public String list(@PathVariable Long patientId,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                       @RequestParam(required = false) Long doctorId,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        var pageable = PageRequest.of(page, 5, Sort.by("visitDate").descending());
        model.addAttribute("patient", patientService.getById(patientId));
        model.addAttribute("records", recordService.getByPatient(patientId, from, to, doctorId, pageable));
        model.addAttribute("doctors", doctorService.getAll());
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("selectedDoctorId", doctorId);
        return "emr/list";
    }

    @GetMapping("/patients/{patientId}/records/new")
    public String newForm(@PathVariable Long patientId, Model model) {
        model.addAttribute("patient", patientService.getById(patientId));
        model.addAttribute("record", new MedicalRecord());
        model.addAttribute("doctors", doctorService.getAll());
        return "emr/form";
    }

    @PostMapping("/patients/{patientId}/records")
    public String create(@PathVariable Long patientId,
                         @Valid @ModelAttribute("record") MedicalRecord record,
                         BindingResult result,
                         @RequestParam(required = false) Long doctorId,
                         Model model,
                         RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("patient", patientService.getById(patientId));
            model.addAttribute("doctors", doctorService.getAll());
            return "emr/form";
        }
        recordService.create(patientId, record, doctorId);
        ra.addFlashAttribute("success", "Запись добавлена.");
        return "redirect:/patients/" + patientId + "/records";
    }

    @GetMapping("/records/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        MedicalRecord record = recordService.getById(id);
        model.addAttribute("record", record);
        model.addAttribute("patient", record.getPatient());
        model.addAttribute("doctors", doctorService.getAll());
        return "emr/form";
    }

    @PostMapping("/records/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("record") MedicalRecord updated,
                         BindingResult result,
                         @RequestParam(required = false) Long doctorId,
                         Model model,
                         RedirectAttributes ra) {
        if (result.hasErrors()) {
            MedicalRecord existing = recordService.getById(id);
            model.addAttribute("patient", existing.getPatient());
            model.addAttribute("doctors", doctorService.getAll());
            return "emr/form";
        }
        if (doctorId != null) {
            doctorService.getAll().stream()
                    .filter(d -> d.getId().equals(doctorId))
                    .findFirst()
                    .ifPresent(updated::setDoctor);
        }
        MedicalRecord saved = recordService.update(id, updated);
        ra.addFlashAttribute("success", "Запись обновлена.");
        return "redirect:/patients/" + saved.getPatient().getId() + "/records";
    }

    @PostMapping("/records/{id}/archive")
    public String archive(@PathVariable Long id, RedirectAttributes ra) {
        MedicalRecord record = recordService.getById(id);
        Long patientId = record.getPatient().getId();
        recordService.archive(id);
        ra.addFlashAttribute("success", "Запись архивирована.");
        return "redirect:/patients/" + patientId + "/records";
    }

    @GetMapping("/emr/archive")
    public String archive(@RequestParam(defaultValue = "0") int page, Model model) {
        var pageable = PageRequest.of(page, 20, Sort.by("updatedAt").descending());
        model.addAttribute("records", recordService.getArchive(pageable));
        return "emr/archive";
    }

    @GetMapping("/records/{id}/voucher")
    public String printVoucher(@PathVariable Long id, Model model) {
        model.addAttribute("record", recordService.getById(id));
        return "emr/print-voucher";
    }

    @GetMapping("/patients/{patientId}/records/extract")
    public String printExtract(@PathVariable Long patientId,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                               Model model) {
        model.addAttribute("patient", patientService.getById(patientId));
        model.addAttribute("records", recordService.getForExtract(patientId, from, to));
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "emr/print-extract";
    }
}
