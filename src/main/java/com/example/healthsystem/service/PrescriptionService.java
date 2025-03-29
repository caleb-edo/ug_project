package com.example.healthsystem.service;

import com.example.healthsystem.model.Prescription;
import com.example.healthsystem.model.Patient;
import com.example.healthsystem.model.Doctor;
import com.example.healthsystem.repositories.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    // Create a new prescription
    @Transactional
    public Prescription createPrescription(Long patientId, Long doctorId, Prescription prescription) {
        Patient patient = patientService.getPatientById(patientId);
        Doctor doctor = doctorService.getDoctorById(doctorId);

        prescription.setPatient(patient);
        prescription.setPrescribingDoctor(doctor);
        prescription.setStatus(Prescription.PrescriptionStatus.ACTIVE);
        prescription.setPrescriptionDate(LocalDate.now());

        // Set default expiration to 1 year from now if not specified
        if (prescription.getExpirationDate() == null) {
            prescription.setExpirationDate(LocalDate.now().plusYears(1));
        }

        // Set default refills to 3 if not specified
        if (prescription.getRefillsRemaining() == null) {
            prescription.setRefillsRemaining(3);
        }

        return prescriptionRepository.save(prescription);
    }

    // Get all prescriptions for a patient
    public List<Prescription> getPrescriptionsForPatient(Long patientId) {
        Patient patient = patientService.getPatientById(patientId);
        return prescriptionRepository.findByPatient(patient);
    }

    // Get all prescriptions for a doctor
    public List<Prescription> getPrescriptionsForDoctor(Long doctorId) {
        Doctor doctor = doctorService.getDoctorById(doctorId);
        return prescriptionRepository.findByPrescribingDoctor(doctor);
    }

    // Request a prescription refill
    @Transactional
    public Prescription requestRefill(Long prescriptionId, Long patientId) {
        Patient patient = patientService.getPatientById(patientId);
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));

        // Validate refill request
        if (prescription.getStatus() != Prescription.PrescriptionStatus.ACTIVE) {
            throw new RuntimeException("Cannot request refill for inactive prescription");
        }

        if (prescription.getRefillsRemaining() <= 0) {
            throw new RuntimeException("No refills remaining");
        }

        // Update prescription status and refill request date
        prescription.setStatus(Prescription.PrescriptionStatus.PENDING_REFILL);
        prescription.setLastRefillRequestDate(LocalDateTime.now());

        return prescriptionRepository.save(prescription);
    }

    // Doctor approves or denies refill
    @Transactional
    public Prescription processRefillRequest(Long prescriptionId, boolean approve) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));

        if (prescription.getStatus() != Prescription.PrescriptionStatus.PENDING_REFILL) {
            throw new RuntimeException("No pending refill to process");
        }

        if (approve) {
            // Decrement refills remaining
            prescription.setRefillsRemaining(prescription.getRefillsRemaining() - 1);
            prescription.setStatus(Prescription.PrescriptionStatus.ACTIVE);
        } else {
            prescription.setStatus(Prescription.PrescriptionStatus.ACTIVE);
        }

        return prescriptionRepository.save(prescription);
    }

    // Get active prescriptions
    public List<Prescription> getActivePrescriptionsForPatient(Long patientId) {
        Patient patient = patientService.getPatientById(patientId);
        return prescriptionRepository.findByPatientAndStatus(patient, Prescription.PrescriptionStatus.ACTIVE);
    }

    // Get prescriptions pending refill for a doctor
    public List<Prescription> getPendingRefillsForDoctor(Long doctorId) {
        Doctor doctor = doctorService.getDoctorById(doctorId);
        return prescriptionRepository.findByPrescribingDoctorAndStatus(doctor, Prescription.PrescriptionStatus.PENDING_REFILL);
    }
}