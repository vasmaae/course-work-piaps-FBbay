package com.hospital.registry.repository;

import com.hospital.registry.model.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByUin(String uin);

    @Query("""
        SELECT p FROM Patient p
        WHERE p.archived = false
        AND (:query IS NULL OR :query = '' OR
             LOWER(p.lastName)  LIKE LOWER(CONCAT('%', :query, '%')) OR
             LOWER(p.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR
             LOWER(p.middleName) LIKE LOWER(CONCAT('%', :query, '%')) OR
             LOWER(p.uin)   LIKE LOWER(CONCAT('%', :query, '%')) OR
             LOWER(p.phone) LIKE LOWER(CONCAT('%', :query, '%')))
        AND (:privilege    IS NULL OR p.privilegeCategory = :privilege)
        AND (:insuranceType IS NULL OR p.insuranceType    = :insuranceType)
        AND (:sectionId    IS NULL OR p.section.id        = :sectionId)
        AND (:gender       IS NULL OR p.gender            = :gender)
        """)
    Page<Patient> filter(
        @Param("query") String query,
        @Param("privilege") Patient.PrivilegeCategory privilege,
        @Param("insuranceType") Patient.InsuranceType insuranceType,
        @Param("sectionId") Long sectionId,
        @Param("gender") Patient.Gender gender,
        Pageable pageable
    );

    Page<Patient> findByArchivedTrue(Pageable pageable);

    long countByArchivedFalse();
}
