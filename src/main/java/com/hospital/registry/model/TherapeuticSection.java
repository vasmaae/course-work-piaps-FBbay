package com.hospital.registry.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "therapeutic_sections")
@Getter @Setter @NoArgsConstructor
public class TherapeuticSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "number", nullable = false, unique = true)
    private Integer number;

    @ManyToMany(mappedBy = "sections", fetch = FetchType.LAZY)
    private List<Doctor> doctors = new ArrayList<>();

    @OneToMany(mappedBy = "section", fetch = FetchType.LAZY)
    private List<Patient> patients;
}
