package com.hospital.registry.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "patients")
@Getter @Setter @NoArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uin", unique = true, nullable = false, length = 20)
    private String uin;

    @NotBlank
    @Size(max = 100)
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Size(max = 100)
    @Column(name = "middle_name", length = 100)
    private String middleName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @NotNull
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Pattern(regexp = "^((\\+7|8)[0-9]{10})?$", message = "Введите номер в формате +7XXXXXXXXXX или 8XXXXXXXXXX")
    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "passport_series", length = 10)
    private String passportSeries;

    @Column(name = "passport_number", length = 20)
    private String passportNumber;

    @Column(name = "passport_issued_by", columnDefinition = "TEXT")
    private String passportIssuedBy;

    @Column(name = "passport_issue_date")
    private LocalDate passportIssueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private TherapeuticSection section;

    @Enumerated(EnumType.STRING)
    @Column(name = "privilege_category")
    private PrivilegeCategory privilegeCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "insurance_type")
    private InsuranceType insuranceType;

    @Column(name = "insurance_number", length = 50)
    private String insuranceNumber;

    @Column(name = "allergies", columnDefinition = "TEXT")
    private String allergies;

    @Column(name = "special_notes", columnDefinition = "TEXT")
    private String specialNotes;

    @Column(name = "is_archived", nullable = false)
    private boolean archived = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MedicalRecord> medicalRecords;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getFullName() {
        return lastName + " " + firstName + (middleName != null ? " " + middleName : "");
    }

    public enum Gender {
        MALE("Мужской"), FEMALE("Женский");

        private final String displayName;
        Gender(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    public enum InsuranceType {
        OMS("ОМС"), DMS("ДМС");

        private final String displayName;
        InsuranceType(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    public enum PrivilegeCategory {
        NONE("Нет"), VETERAN("Ветеран"), DISABLED("Инвалид"), PENSIONER("Пенсионер"), OTHER("Прочее");

        private final String displayName;
        PrivilegeCategory(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
}
