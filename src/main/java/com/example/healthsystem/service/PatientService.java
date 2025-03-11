package com.example.healthsystem.service;

import com.example.healthsystem.model.Doctor;
import com.example.healthsystem.model.Patient;
import com.example.healthsystem.repositories.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    public Patient getPatientByUsername(String username) {
        return patientRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Patient not found with username: " + username));
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));
    }

    public Map<String, String> getHealthMetrics(Long patientId) {
        Map<String, String> metrics = new HashMap<>();
        metrics.put("Blood Pressure", "120/80 mmHg");
        metrics.put("Heart Rate", "72 bpm");
        metrics.put("Blood Sugar", "90 mg/dL");
        return metrics;
    }

    public Map<String, String> getTrendAnalysis(Long patientId) {
        Map<String, String> trends = new HashMap<>();
        trends.put("Blood Pressure Trend", "Stable");
        trends.put("Heart Rate Trend", "Slightly Increasing");
        trends.put("Blood Sugar Trend", "Decreasing");
        return trends;
    }
    public List<Patient> getUnassignedPatients() {
        return patientRepository.findByDoctorIsNull();
    }

    // Assign a patient to a doctor
    public void assignPatientToDoctor(Long patientId, Doctor doctor) {
        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        if (patientOpt.isPresent()) {
            Patient patient = patientOpt.get();
            patient.setDoctor(doctor);
            patientRepository.save(patient);
        }
    }
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }
}