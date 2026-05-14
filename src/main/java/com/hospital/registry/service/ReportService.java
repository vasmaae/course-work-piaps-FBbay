package com.hospital.registry.service;

import com.hospital.registry.model.Doctor;
import com.hospital.registry.model.MedicalRecord;
import com.hospital.registry.repository.DoctorRepository;
import com.hospital.registry.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final MedicalRecordRepository recordRepository;
    private final DoctorRepository doctorRepository;

    @Transactional(readOnly = true)
    public Map<Doctor, Long> getWorkloadReport(LocalDate from, LocalDate to) {
        List<Doctor> doctors = doctorRepository.findAllByOrderByFullName();
        Map<Doctor, Long> result = new LinkedHashMap<>();
        for (Doctor doctor : doctors) {
            long count = recordRepository.countByDoctorIdAndVisitDateBetween(doctor.getId(), from, to);
            result.put(doctor, count);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getRecordsByPeriod(LocalDate from, LocalDate to) {
        return recordRepository.findByVisitDateBetween(from, to);
    }
}
