package com.example.healthsystem.service;

import com.example.healthsystem.model.Patient;
import com.example.healthsystem.repositories.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    public Patient getPatientByUsername(String username) {
        return patientRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Patient not found with username: " + username));
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
}