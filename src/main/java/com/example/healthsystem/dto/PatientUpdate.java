package com.example.healthsystem.dto;

import java.time.LocalDateTime;

public class PatientUpdate {
    private Long patientId;
    private String patientName;
    private String type;
    private String details;
    private LocalDateTime date;

    public PatientUpdate(Long patientId, String patientName, String type, String details, LocalDateTime date) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.type = type;
        this.details = details;
        this.date = date;
    }

    // Getters and Setters
    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}