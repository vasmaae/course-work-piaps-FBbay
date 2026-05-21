package com.hospital.registry.controller;

import com.hospital.registry.model.Doctor;
import com.hospital.registry.model.MedicalRecord;
import com.hospital.registry.model.Patient;
import com.hospital.registry.service.DoctorService;
import com.hospital.registry.service.MedicalRecordService;
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
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.List;

@Slf4j
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
        log.debug("EMR list: patientId={}, from={}, to={}, doctorId={}, page={}", patientId, from, to, doctorId, page);
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
        log.debug("Opening new EMR form for patientId={}", patientId);
        Patient patient = patientService.getById(patientId);
        model.addAttribute("patient", patient);
        model.addAttribute("record", new MedicalRecord());
        model.addAttribute("doctors", doctorsFor(patient));
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
            log.warn("EMR creation for patientId={} failed validation: {} errors", patientId, result.getErrorCount());
            Patient patient = patientService.getById(patientId);
            model.addAttribute("patient", patient);
            model.addAttribute("doctors", doctorsFor(patient));
            return "emr/form";
        }
        log.info("Creating EMR for patientId={}, visitDate={}, doctorId={}", patientId, record.getVisitDate(), doctorId);
        recordService.create(patientId, record, doctorId);
        log.info("EMR created for patientId={}", patientId);
        ra.addFlashAttribute("success", "Запись добавлена.");
        return "redirect:/patients/" + patientId + "/records";
    }

    @GetMapping("/records/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        log.debug("Opening edit form for EMR id={}", id);
        MedicalRecord record = recordService.getById(id);
        Patient patient = patientService.getById(record.getPatient().getId());
        model.addAttribute("record", record);
        model.addAttribute("patient", patient);
        model.addAttribute("doctors", doctorsFor(patient));
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
            log.warn("EMR update id={} failed validation: {} errors", id, result.getErrorCount());
            MedicalRecord existing = recordService.getById(id);
            Patient patient = patientService.getById(existing.getPatient().getId());
            model.addAttribute("patient", patient);
            model.addAttribute("doctors", doctorsFor(patient));
            return "emr/form";
        }
        log.info("Updating EMR id={}", id);
        if (doctorId != null) {
            doctorService.getAll().stream()
                    .filter(d -> d.getId().equals(doctorId))
                    .findFirst()
                    .ifPresent(updated::setDoctor);
        }
        MedicalRecord saved = recordService.update(id, updated);
        log.info("EMR id={} updated", id);
        ra.addFlashAttribute("success", "Запись обновлена.");
        return "redirect:/patients/" + saved.getPatient().getId() + "/records";
    }

    @PostMapping("/records/{id}/archive")
    public String archive(@PathVariable Long id, RedirectAttributes ra) {
        MedicalRecord record = recordService.getById(id);
        Long patientId = record.getPatient().getId();
        log.info("Archiving EMR id={} (patientId={})", id, patientId);
        recordService.archive(id);
        log.info("EMR id={} archived", id);
        ra.addFlashAttribute("success", "Запись архивирована.");
        return "redirect:/patients/" + patientId + "/records";
    }

    @GetMapping("/emr/archive")
    public String archive(@RequestParam(defaultValue = "0") int page, Model model) {
        log.debug("EMR archive page={}", page);
        var pageable = PageRequest.of(page, 20, Sort.by("updatedAt").descending());
        model.addAttribute("records", recordService.getArchive(pageable));
        return "emr/archive";
    }

    @PostMapping("/records/{id}/restore")
    public String restore(@PathVariable Long id, RedirectAttributes ra) {
        log.info("Restoring EMR id={}", id);
        recordService.restore(id);
        log.info("EMR id={} restored", id);
        ra.addFlashAttribute("success", "Запись восстановлена.");
        return "redirect:/emr/archive";
    }

    @GetMapping("/records/{id}/voucher")
    public String printVoucher(@PathVariable Long id, Model model) {
        log.debug("Printing voucher for EMR id={}", id);
        model.addAttribute("record", recordService.getById(id));
        return "emr/print-voucher";
    }

    private List<Doctor> doctorsFor(Patient patient) {
        return patient.getSection() != null
                ? doctorService.getDoctorsForSection(patient.getSection().getId())
                : doctorService.getAll();
    }

    @GetMapping("/patients/{patientId}/records/extract")
    public String printExtract(@PathVariable Long patientId,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                               Model model) {
        log.debug("Printing extract for patientId={}, from={}, to={}", patientId, from, to);
        model.addAttribute("patient", patientService.getById(patientId));
        model.addAttribute("records", recordService.getForExtract(patientId, from, to));
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "emr/print-extract";
    }
}
