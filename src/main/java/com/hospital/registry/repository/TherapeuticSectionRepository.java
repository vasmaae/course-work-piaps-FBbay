package com.hospital.registry.repository;

import com.hospital.registry.model.TherapeuticSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TherapeuticSectionRepository extends JpaRepository<TherapeuticSection, Long> {
    List<TherapeuticSection> findAllByOrderByNumber();
}
