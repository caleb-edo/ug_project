package com.example.healthsystem.service;

import com.example.healthsystem.model.Doctor;
import com.example.healthsystem.model.Patient;
import com.example.healthsystem.dto.PatientStatistics;
import com.example.healthsystem.dto.PatientUpdate;
import com.example.healthsystem.repositories.DoctorRepository;
import com.example.healthsystem.repositories.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    public Doctor getDoctorByUsername(String username) {
        return doctorRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Doctor not found with username: " + username));
    }
    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));
    }


    public PatientStatistics getPatientStatistics(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        PatientStatistics stats = new PatientStatistics();
        List<Patient> patients = doctor.getPatients();

        stats.setTotalPatients(patients.size());
        stats.setRequireAttention(calculatePatientsNeedingAttention(patients));
        stats.setPendingAppointments(calculatePendingAppointments(patients));

        return stats;
    }

    public List<PatientUpdate> getRecentPatientUpdates(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        return generatePatientUpdates(doctor.getPatients());
    }

    private int calculatePatientsNeedingAttention(List<Patient> patients) {
        return (int) patients.stream()
                .filter(patient -> {
                    boolean hasCurrentMedications = patient.getCurrentMedications() != null &&
                            !patient.getCurrentMedications().isEmpty();
                    boolean hasRecentHistory = patient.getMedicalHistory() != null &&
                            !patient.getMedicalHistory().isEmpty();
                    boolean hasAllergies = patient.getAllergies() != null &&
                            !patient.getAllergies().isEmpty();

                    return hasCurrentMedications || hasRecentHistory || hasAllergies;
                })
                .count();
    }

    private int calculatePendingAppointments(List<Patient> patients) {
        return (int) Math.ceil(patients.size() * 0.2);
    }

    private List<PatientUpdate> generatePatientUpdates(List<Patient> patients) {
        List<PatientUpdate> updates = new ArrayList<>();

        for (Patient patient : patients) {
            if (patient.getCurrentMedications() != null && !patient.getCurrentMedications().isEmpty()) {
                updates.add(new PatientUpdate(
                        patient.getId(),
                        patient.getFullName(),
                        "Medication Update",
                        "Current medications: " + patient.getCurrentMedications(),
                        LocalDateTime.now()
                ));
            }

            if (patient.getMedicalHistory() != null && !patient.getMedicalHistory().isEmpty()) {
                updates.add(new PatientUpdate(
                        patient.getId(),
                        patient.getFullName(),
                        "Medical History",
                        "Updated medical history",
                        LocalDateTime.now().minusDays(1)
                ));
            }
        }

        return updates.stream()
                .sorted((u1, u2) -> u2.getDate().compareTo(u1.getDate()))
                .limit(10)
                .collect(Collectors.toList());
    }
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }
}