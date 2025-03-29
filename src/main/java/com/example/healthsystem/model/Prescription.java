package com.example.healthsystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "prescriptions")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Medication name is required")
    @Size(max = 100, message = "Medication name must be less than 100 characters")
    private String medicationName;

    @NotNull(message = "Dosage is required")
    @Size(max = 50, message = "Dosage must be less than 50 characters")
    private String dosage;

    @NotNull(message = "Frequency is required")
    @Size(max = 50, message = "Frequency must be less than 50 characters")
    private String frequency;

    @NotNull(message = "Prescription date is required")
    private LocalDate prescriptionDate;

    @NotNull(message = "Expiration date is required")
    private LocalDate expirationDate;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "prescribing_doctor_id", nullable = false)
    private Doctor prescribingDoctor;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status is required")
    private PrescriptionStatus status;

    @Column(length = 500)
    private String notes;

    private Integer refillsRemaining;

    private LocalDateTime lastRefillRequestDate;

    // Enum for prescription status
    public enum PrescriptionStatus {
        ACTIVE,       // Currently valid prescription
        EXPIRED,      // Prescription has passed its expiration date
        DISCONTINUED, // Doctor has stopped the prescription
        PENDING_REFILL // Patient has requested a refill
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public LocalDate getPrescriptionDate() {
        return prescriptionDate;
    }

    public void setPrescriptionDate(LocalDate prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getPrescribingDoctor() {
        return prescribingDoctor;
    }

    public void setPrescribingDoctor(Doctor prescribingDoctor) {
        this.prescribingDoctor = prescribingDoctor;
    }

    public PrescriptionStatus getStatus() {
        return status;
    }

    public void setStatus(PrescriptionStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getRefillsRemaining() {
        return refillsRemaining;
    }

    public void setRefillsRemaining(Integer refillsRemaining) {
        this.refillsRemaining = refillsRemaining;
    }

    public LocalDateTime getLastRefillRequestDate() {
        return lastRefillRequestDate;
    }

    public void setLastRefillRequestDate(LocalDateTime lastRefillRequestDate) {
        this.lastRefillRequestDate = lastRefillRequestDate;
    }
}