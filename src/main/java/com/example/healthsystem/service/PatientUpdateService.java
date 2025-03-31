package com.example.healthsystem.service;

import com.example.healthsystem.dto.PatientUpdate;
import com.example.healthsystem.model.*;
import com.example.healthsystem.repositories.AppointmentRepository;
import com.example.healthsystem.repositories.MessageRepository;
import com.example.healthsystem.repositories.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientUpdateService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private MessageRepository messageRepository;

    public List<PatientUpdate> getRecentUpdatesForDoctor(Doctor doctor) {
        List<PatientUpdate> allUpdates = new ArrayList<>();

        // Add recent appointment updates
        List<Appointment> recentAppointments = appointmentRepository.findByDoctor(doctor);
        for (Appointment appointment : recentAppointments) {
            // Only include recent appointments (modified in last 7 days)
            if (appointment.getUpdatedAt() != null &&
                    appointment.getUpdatedAt().isAfter(LocalDateTime.now().minusDays(7))) {
                String details = getAppointmentStatusDetails(appointment);
                allUpdates.add(new PatientUpdate(
                        appointment.getPatient().getId(),
                        appointment.getPatient().getFullName(),
                        "Appointment",
                        details,
                        appointment.getUpdatedAt()
                ));
            }
        }

        // Add recent prescription updates
        List<Prescription> recentPrescriptions = prescriptionRepository.findByPrescribingDoctor(doctor);
        for (Prescription prescription : recentPrescriptions) {
            // Include newly created prescriptions or refill requests
            if (prescription.getLastRefillRequestDate() != null &&
                    prescription.getLastRefillRequestDate().isAfter(LocalDateTime.now().minusDays(7))) {
                String details = "Refill requested for " + prescription.getMedicationName();
                allUpdates.add(new PatientUpdate(
                        prescription.getPatient().getId(),
                        prescription.getPatient().getFullName(),
                        "Prescription",
                        details,
                        prescription.getLastRefillRequestDate()
                ));
            }
        }

        // Add recent message updates
        List<Message> recentMessages = messageRepository.findByReceiverOrderByTimestampDesc(doctor);
        for (Message message : recentMessages) {
            // Only include recent messages (within last 7 days)
            if (message.getTimestamp().isAfter(LocalDateTime.now().minusDays(7))) {
                // Check if sender is a patient
                if (message.getSender() instanceof Patient) {
                    Patient patient = (Patient) message.getSender();
                    String details = "New message: " + getTruncatedContent(message.getContent());
                    allUpdates.add(new PatientUpdate(
                            patient.getId(),
                            patient.getFullName(),
                            "Message",
                            details,
                            message.getTimestamp()
                    ));
                }
            }
        }

        // Sort all updates by date (most recent first) and limit the result
        return allUpdates.stream()
                .sorted(Comparator.comparing(PatientUpdate::getDate).reversed())
                .limit(10)  // Return only 10 most recent
                .collect(Collectors.toList());
    }

    // Helper methods for formatting
    private String getAppointmentStatusDetails(Appointment appointment) {
        switch (appointment.getStatus()) {
            case REQUESTED:
                return "New appointment requested for " + formatDateTime(appointment.getAppointmentDateTime());
            case CONFIRMED:
                return "Appointment confirmed for " + formatDateTime(appointment.getAppointmentDateTime());
            case COMPLETED:
                return "Appointment completed on " + formatDateTime(appointment.getAppointmentDateTime());
            case CANCELLED:
                return "Appointment cancelled that was scheduled for " + formatDateTime(appointment.getAppointmentDateTime());
            case RESCHEDULED:
                return "Appointment rescheduled to " + formatDateTime(appointment.getAppointmentDateTime());
            default:
                return "Appointment updated for " + formatDateTime(appointment.getAppointmentDateTime());
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a"));
    }

    private String getTruncatedContent(String content) {
        if (content.length() <= 50) {
            return content;
        }
        return content.substring(0, 47) + "...";
    }
}