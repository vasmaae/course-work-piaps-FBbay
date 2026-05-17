package com.hospital.registry.service;

import com.hospital.registry.model.Doctor;
import com.hospital.registry.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

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
}
