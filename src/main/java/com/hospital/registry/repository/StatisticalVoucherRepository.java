package com.hospital.registry.repository;

import com.hospital.registry.model.StatisticalVoucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StatisticalVoucherRepository extends JpaRepository<StatisticalVoucher, Long> {

    List<StatisticalVoucher> findByVisitDateBetween(LocalDate from, LocalDate to);

    Page<StatisticalVoucher> findByDoctorIdAndVisitDateBetween(Long doctorId, LocalDate from, LocalDate to, Pageable pageable);
}
