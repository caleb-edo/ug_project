package com.example.healthsystem.dto;

public class PatientStatistics {
    private int totalPatients;
    private int requireAttention;
    private int pendingAppointments;

    // Getters and Setters
    public int getTotalPatients() {
        return totalPatients;
    }

    public void setTotalPatients(int totalPatients) {
        this.totalPatients = totalPatients;
    }

    public int getRequireAttention() {
        return requireAttention;
    }

    public void setRequireAttention(int requireAttention) {
        this.requireAttention = requireAttention;
    }

    public int getPendingAppointments() {
        return pendingAppointments;
    }

    public void setPendingAppointments(int pendingAppointments) {
        this.pendingAppointments = pendingAppointments;
    }
}