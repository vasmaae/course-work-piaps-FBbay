package com.hospital.registry.service;

import com.hospital.registry.audit.AuditService;
import com.hospital.registry.model.Patient;
import com.hospital.registry.model.TherapeuticSection;
import com.hospital.registry.repository.PatientRepository;
import com.hospital.registry.repository.TherapeuticSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final TherapeuticSectionRepository sectionRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Page<Patient> search(String query,
                                Patient.PrivilegeCategory privilege,
                                Patient.InsuranceType insuranceType,
                                Long sectionId,
                                Patient.Gender gender,
                                Pageable pageable) {
        String q = (query == null || query.isBlank()) ? null : query.trim();
        return patientRepository.filter(q, privilege, insuranceType, sectionId, gender, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Patient> getArchive(Pageable pageable) {
        return patientRepository.findByArchivedTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Patient getById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Patient not found: " + id));
    }

    @Transactional
    public Patient create(Patient patient) {
        patient.setUin(generateUin());
        Patient saved = patientRepository.save(patient);
        auditService.log("Patient", saved.getId(), "CREATE", null, saved.getFullName());
        return saved;
    }

    @Transactional
    public Patient update(Long id, Patient updated) {
        Patient existing = getById(id);
        String oldValue = existing.getFullName();

        existing.setLastName(updated.getLastName());
        existing.setFirstName(updated.getFirstName());
        existing.setMiddleName(updated.getMiddleName());
        existing.setGender(updated.getGender());
        existing.setBirthDate(updated.getBirthDate());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setAddress(updated.getAddress());
        existing.setPassportSeries(updated.getPassportSeries());
        existing.setPassportNumber(updated.getPassportNumber());
        existing.setPassportIssuedBy(updated.getPassportIssuedBy());
        existing.setPassportIssueDate(updated.getPassportIssueDate());
        existing.setPrivilegeCategory(updated.getPrivilegeCategory());
        existing.setInsuranceType(updated.getInsuranceType());
        existing.setInsuranceNumber(updated.getInsuranceNumber());
        existing.setAllergies(updated.getAllergies());
        existing.setSpecialNotes(updated.getSpecialNotes());

        Patient saved = patientRepository.save(existing);
        auditService.log("Patient", id, "UPDATE", oldValue, saved.getFullName());
        return saved;
    }

    @Transactional
    public void attachSection(Long patientId, Long sectionId) {
        Patient patient = getById(patientId);
        TherapeuticSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new NoSuchElementException("Section not found: " + sectionId));
        String old = patient.getSection() != null ? patient.getSection().getName() : null;
        patient.setSection(section);
        patientRepository.save(patient);
        auditService.log("Patient", patientId, "ATTACH_SECTION", old, section.getName());
    }

    @Transactional
    public void detachSection(Long patientId) {
        Patient patient = getById(patientId);
        String old = patient.getSection() != null ? patient.getSection().getName() : null;
        patient.setSection(null);
        patientRepository.save(patient);
        auditService.log("Patient", patientId, "DETACH_SECTION", old, null);
    }

    @Transactional
    public void archive(Long id) {
        Patient patient = getById(id);
        patient.setArchived(true);
        patientRepository.save(patient);
        auditService.log("Patient", id, "ARCHIVE", "active", "archived");
    }

    @Transactional
    public void restore(Long id) {
        Patient patient = getById(id);
        patient.setArchived(false);
        patientRepository.save(patient);
        auditService.log("Patient", id, "RESTORE", "archived", "active");
    }

    private String generateUin() {
        long count = patientRepository.countByArchivedFalse() + patientRepository.count();
        return String.format("%d-%06d", Year.now().getValue(), count + 1);
    }
}
