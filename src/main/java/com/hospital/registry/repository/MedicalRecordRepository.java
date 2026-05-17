package com.hospital.registry.repository;

import com.hospital.registry.model.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    Page<MedicalRecord> findByPatientIdAndArchivedFalse(Long patientId, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("""
        SELECT r FROM MedicalRecord r
        WHERE r.patient.id = :patientId AND r.archived = false
        AND (:from IS NULL OR r.visitDate >= :from)
        AND (:to IS NULL OR r.visitDate <= :to)
        AND (:doctorId IS NULL OR r.doctor.id = :doctorId)
        """)
    Page<MedicalRecord> filter(
        @org.springframework.data.repository.query.Param("patientId") Long patientId,
        @org.springframework.data.repository.query.Param("from") java.time.LocalDate from,
        @org.springframework.data.repository.query.Param("to") java.time.LocalDate to,
        @org.springframework.data.repository.query.Param("doctorId") Long doctorId,
        Pageable pageable);

    Page<MedicalRecord> findByArchivedTrue(Pageable pageable);

    List<MedicalRecord> findByVisitDateBetween(LocalDate from, LocalDate to);

    long countByDoctorIdAndVisitDateBetween(Long doctorId, LocalDate from, LocalDate to);

    long countByVisitDateAndArchivedFalse(LocalDate visitDate);

    @org.springframework.data.jpa.repository.Query("""
        SELECT r FROM MedicalRecord r
        WHERE r.patient.id = :patientId AND r.archived = false
        AND (:from IS NULL OR r.visitDate >= :from)
        AND (:to   IS NULL OR r.visitDate <= :to)
        ORDER BY r.visitDate DESC
        """)
    List<MedicalRecord> findForExtract(
        @org.springframework.data.repository.query.Param("patientId") Long patientId,
        @org.springframework.data.repository.query.Param("from") LocalDate from,
        @org.springframework.data.repository.query.Param("to") LocalDate to);

    @org.springframework.data.jpa.repository.Query("""
        SELECT r.icdCode, r.diagnosis, COUNT(r)
        FROM MedicalRecord r
        WHERE r.archived = false AND r.icdCode IS NOT NULL
        AND r.visitDate BETWEEN :from AND :to
        GROUP BY r.icdCode, r.diagnosis
        ORDER BY COUNT(r) DESC
        """)
    List<Object[]> getDiagnosisStats(
        @org.springframework.data.repository.query.Param("from") LocalDate from,
        @org.springframework.data.repository.query.Param("to") LocalDate to);
}
