package com.hospital.registry.service;

import com.hospital.registry.model.Doctor;
import com.hospital.registry.model.TherapeuticSection;
import com.hospital.registry.repository.DoctorRepository;
import com.hospital.registry.repository.TherapeuticSectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final TherapeuticSectionRepository sectionRepository;

    @Transactional(readOnly = true)
    public List<Doctor> getAll() {
        log.debug("Fetching all doctors");
        return doctorRepository.findAllByOrderByFullName();
    }

    @Transactional(readOnly = true)
    public Doctor getById(Long id) {
        log.debug("Fetching doctor id={}", id);
        return doctorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Doctor not found: " + id));
    }

    @Transactional
    public Doctor save(Doctor doctor) {
        boolean isNew = doctor.getId() == null;
        log.info("{} doctor: '{}'", isNew ? "Creating" : "Updating", doctor.getFullName());
        Doctor saved = doctorRepository.save(doctor);
        log.info("Doctor id={} saved", saved.getId());
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting doctor id={}", id);
        doctorRepository.deleteById(id);
        log.info("Doctor id={} deleted", id);
    }

    @Transactional(readOnly = true)
    public List<TherapeuticSection> getSectionsNotAssignedToDoctor(Long doctorId) {
        Doctor doctor = getById(doctorId);
        Set<Long> assignedIds = doctor.getSections().stream()
                .map(TherapeuticSection::getId)
                .collect(Collectors.toSet());
        return sectionRepository.findAllByOrderByNumber().stream()
                .filter(s -> !assignedIds.contains(s.getId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Doctor> getDoctorsForSection(Long sectionId) {
        return sectionRepository.findById(sectionId)
                .map(s -> s.getDoctors().isEmpty() ? getAll() : new ArrayList<>(s.getDoctors()))
                .orElseGet(this::getAll);
    }

    @Transactional
    public void attachSection(Long doctorId, Long sectionId) {
        Doctor doctor = getById(doctorId);
        TherapeuticSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new NoSuchElementException("Section not found: " + sectionId));
        boolean alreadyAttached = doctor.getSections().stream()
                .anyMatch(s -> s.getId().equals(sectionId));
        if (!alreadyAttached) {
            doctor.getSections().add(section);
            doctorRepository.save(doctor);
        }
        log.info("Section id={} attached to doctor id={}", sectionId, doctorId);
    }

    @Transactional
    public void detachSection(Long doctorId, Long sectionId) {
        Doctor doctor = getById(doctorId);
        doctor.getSections().removeIf(s -> s.getId().equals(sectionId));
        doctorRepository.save(doctor);
        log.info("Section id={} detached from doctor id={}", sectionId, doctorId);
    }
}
