package com.hospital.registry.service;

import com.hospital.registry.audit.AuditService;
import com.hospital.registry.model.MedicalRecord;
import com.hospital.registry.model.Patient;
import com.hospital.registry.repository.AppUserRepository;
import com.hospital.registry.repository.DoctorRepository;
import com.hospital.registry.repository.MedicalRecordRepository;
import com.hospital.registry.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository recordRepository;
    private final PatientRepository patientRepository;
    private final AppUserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Page<MedicalRecord> getByPatient(Long patientId, LocalDate from, LocalDate to, Long doctorId, Pageable pageable) {
        log.debug("Fetching EMR for patientId={}, from={}, to={}, doctorId={}", patientId, from, to, doctorId);
        if (from == null && to == null && doctorId == null) {
            return recordRepository.findByPatientIdAndArchivedFalse(patientId, pageable);
        }
        return recordRepository.filter(patientId, from, to, doctorId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<MedicalRecord> getArchive(Pageable pageable) {
        log.debug("Fetching EMR archive");
        return recordRepository.findByArchivedTrue(pageable);
    }

    @Transactional(readOnly = true)
    public MedicalRecord getById(Long id) {
        log.debug("Fetching EMR id={}", id);
        return recordRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Record not found: " + id));
    }

    @Transactional
    public MedicalRecord create(Long patientId, MedicalRecord record, Long doctorId) {
        log.info("Creating EMR for patientId={}, visitDate={}, doctorId={}", patientId, record.getVisitDate(), doctorId);
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NoSuchElementException("Patient not found: " + patientId));
        record.setPatient(patient);

        if (doctorId != null) {
            doctorRepository.findById(doctorId).ifPresent(record::setDoctor);
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        userRepository.findByUsername(username).ifPresent(record::setCreatedBy);

        MedicalRecord saved = recordRepository.save(record);
        auditService.log("MedicalRecord", saved.getId(), "CREATE", null,
                "Patient: " + patient.getUin() + ", Date: " + saved.getVisitDate());
        log.info("EMR created: id={}, patientId={}", saved.getId(), patientId);
        return saved;
    }

    @Transactional
    public MedicalRecord update(Long id, MedicalRecord updated) {
        log.info("Updating EMR id={}", id);
        MedicalRecord existing = getById(id);
        String old = existing.getDiagnosis();

        existing.setVisitDate(updated.getVisitDate());
        existing.setComplaints(updated.getComplaints());
        existing.setDiagnosis(updated.getDiagnosis());
        existing.setIcdCode(updated.getIcdCode());
        existing.setTreatment(updated.getTreatment());
        existing.setNotes(updated.getNotes());
        existing.setDoctor(updated.getDoctor());

        MedicalRecord saved = recordRepository.save(existing);
        auditService.log("MedicalRecord", id, "UPDATE", old, saved.getDiagnosis());
        log.info("EMR id={} updated", id);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getForExtract(Long patientId, LocalDate from, LocalDate to) {
        log.debug("Fetching EMR extract for patientId={}, from={}, to={}", patientId, from, to);
        return recordRepository.findForExtract(patientId, from, to);
    }

    @Transactional
    public void archive(Long id) {
        log.info("Archiving EMR id={}", id);
        MedicalRecord record = getById(id);
        record.setArchived(true);
        recordRepository.save(record);
        auditService.log("MedicalRecord", id, "ARCHIVE", "active", "archived");
        log.info("EMR id={} archived", id);
    }

    @Transactional
    public void restore(Long id) {
        log.info("Restoring EMR id={}", id);
        MedicalRecord record = getById(id);
        record.setArchived(false);
        recordRepository.save(record);
        auditService.log("MedicalRecord", id, "RESTORE", "archived", "active");
        log.info("EMR id={} restored", id);
    }
}
