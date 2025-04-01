package com.example.healthsystem.controllers;

import com.example.healthsystem.dto.AppointmentDTO;
import com.example.healthsystem.model.Appointment;
import com.example.healthsystem.model.Doctor;
import com.example.healthsystem.model.Patient;
import com.example.healthsystem.service.AppointmentService;
import com.example.healthsystem.service.DoctorService;
import com.example.healthsystem.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    // View appointment dashboard (different views for patient/doctor)
    @GetMapping
    public String viewAppointments(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        if (role.equals("ROLE_DOCTOR")) {
            Doctor doctor = doctorService.getDoctorByUsername(username);
            List<Appointment> pendingAppointments = appointmentService.getPendingAppointmentsByDoctor(doctor);
            List<Appointment> confirmedAppointments = appointmentService.getConfirmedAppointmentsByDoctor(doctor);
            // Also fetch rescheduled appointments
            List<Appointment> rescheduledAppointments = appointmentService.getRescheduledAppointmentsByDoctor(doctor);

            // Add rescheduled appointments to pending for display
            pendingAppointments.addAll(rescheduledAppointments);

            model.addAttribute("doctor", doctor);
            model.addAttribute("pendingAppointments", pendingAppointments);
            model.addAttribute("confirmedAppointments", confirmedAppointments);
            return "doctor-appointments";
        } else {
            Patient patient = patientService.getPatientByUsername(username);
            List<Appointment> pendingAppointments = appointmentService.getPendingAppointmentsByPatient(patient);
            List<Appointment> confirmedAppointments = appointmentService.getConfirmedAppointmentsByPatient(patient);
            // Also fetch rescheduled appointments
            List<Appointment> rescheduledAppointments = appointmentService.getRescheduledAppointmentsByPatient(patient);

            // Add rescheduled appointments to pending for display
            pendingAppointments.addAll(rescheduledAppointments);

            model.addAttribute("patient", patient);
            model.addAttribute("pendingAppointments", pendingAppointments);
            model.addAttribute("confirmedAppointments", confirmedAppointments);
            return "patient-appointments";
        }
    }
    private AppointmentDTO convertToDTO(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setPatientId(appointment.getPatient().getId());
        dto.setPatientName(appointment.getPatient().getFullName());
        dto.setDoctorId(appointment.getDoctor().getId());
        dto.setDoctorName(appointment.getDoctor().getFullName());

        // Format date and time using the static formatter method in AppointmentDTO
        dto.setAppointmentDate(AppointmentDTO.formatDateTime(appointment.getAppointmentDateTime(), "yyyy-MM-dd"));
        dto.setAppointmentTime(AppointmentDTO.formatDateTime(appointment.getAppointmentDateTime(), "HH:mm"));

        dto.setNotes(appointment.getNotes());
        dto.setStatus(appointment.getStatus().toString());
        dto.setCreatedAt(AppointmentDTO.formatDateTime(appointment.getCreatedAt(), "yyyy-MM-dd HH:mm"));

        // Add updated timestamp as well
        if (appointment.getUpdatedAt() != null) {
            dto.setUpdatedAt(AppointmentDTO.formatDateTime(appointment.getUpdatedAt(), "yyyy-MM-dd HH:mm"));
        }

        return dto;
    }

    // Patient creates a new appointment request
    @PostMapping("/request")
    public ResponseEntity<Map<String, Object>> requestAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appointmentDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime appointmentTime,
            @RequestParam(required = false) String notes) {

        Map<String, Object> response = new HashMap<>();
        try {
            Patient patient = patientService.getPatientByUsername(userDetails.getUsername());
            Doctor doctor = doctorService.getDoctorById(doctorId);

            Appointment appointment = new Appointment();
            appointment.setPatient(patient);
            appointment.setDoctor(doctor);
            appointment.setAppointmentDateTime(LocalDateTime.of(appointmentDate, appointmentTime));
            appointment.setNotes(notes);

            Appointment saved = appointmentService.createAppointment(appointment);

            response.put("success", true);
            response.put("message", "Appointment request submitted successfully!");
            response.put("appointmentId", saved.getId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to request appointment: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Doctor confirms an appointment
    @PostMapping("/{id}/confirm")
    public ResponseEntity<Map<String, Object>> confirmAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Map<String, Object> response = new HashMap<>();
        try {
            Doctor doctor = doctorService.getDoctorByUsername(userDetails.getUsername());
            Optional<Appointment> appointmentOpt = appointmentService.getAppointmentById(id);

            if (!appointmentOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Appointment not found");
                return ResponseEntity.badRequest().body(response);
            }

            Appointment appointment = appointmentOpt.get();
            if (!appointment.getDoctor().getId().equals(doctor.getId())) {
                response.put("success", false);
                response.put("message", "You can only confirm your own appointments");
                return ResponseEntity.badRequest().body(response);
            }

            appointmentService.confirmAppointment(id);

            response.put("success", true);
            response.put("message", "Appointment confirmed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to confirm appointment: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Patient or doctor cancels an appointment
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Map<String, Object> response = new HashMap<>();
        try {
            String username = userDetails.getUsername();
            String role = userDetails.getAuthorities().iterator().next().getAuthority();

            Optional<Appointment> appointmentOpt = appointmentService.getAppointmentById(id);
            if (!appointmentOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Appointment not found");
                return ResponseEntity.badRequest().body(response);
            }

            Appointment appointment = appointmentOpt.get();

            // Verify that the user is authorized to cancel this appointment
            boolean isAuthorized = false;
            if (role.equals("ROLE_DOCTOR") && appointment.getDoctor().getUsername().equals(username)) {
                isAuthorized = true;
            } else if (role.equals("ROLE_PATIENT") && appointment.getPatient().getUsername().equals(username)) {
                isAuthorized = true;
            }

            if (!isAuthorized) {
                response.put("success", false);
                response.put("message", "You are not authorized to cancel this appointment");
                return ResponseEntity.badRequest().body(response);
            }

            appointmentService.cancelAppointment(id);

            response.put("success", true);
            response.put("message", "Appointment cancelled successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to cancel appointment: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Patient reschedules an appointment
    @PostMapping("/{id}/reschedule")
    public ResponseEntity<Map<String, Object>> rescheduleAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime newTime) {

        Map<String, Object> response = new HashMap<>();
        try {
            Patient patient = patientService.getPatientByUsername(userDetails.getUsername());
            Optional<Appointment> appointmentOpt = appointmentService.getAppointmentById(id);

            if (!appointmentOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Appointment not found");
                return ResponseEntity.badRequest().body(response);
            }

            Appointment appointment = appointmentOpt.get();
            if (!appointment.getPatient().getId().equals(patient.getId())) {
                response.put("success", false);
                response.put("message", "You can only reschedule your own appointments");
                return ResponseEntity.badRequest().body(response);
            }

            appointmentService.rescheduleAppointment(id, LocalDateTime.of(newDate, newTime));

            response.put("success", true);
            response.put("message", "Appointment rescheduled successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to reschedule appointment: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Get available time slots for a doctor on a specific date
    @GetMapping("/available-slots")
    public ResponseEntity<Map<String, Object>> getAvailableSlots(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Map<String, Object> response = new HashMap<>();
        try {
            Doctor doctor = doctorService.getDoctorById(doctorId);
            List<Appointment> existingAppointments = appointmentService.getDoctorAppointmentsForDate(doctor, date);

            // Generate all time slots (e.g., every 30 min from 9 AM to 5 PM)
            List<LocalTime> allSlots = generateTimeSlots(9, 17, 30);

            // Remove booked slots
            List<LocalTime> bookedSlots = existingAppointments.stream()
                    .map(app -> app.getAppointmentDateTime().toLocalTime())
                    .collect(Collectors.toList());

            List<LocalTime> availableSlots = allSlots.stream()
                    .filter(time -> !bookedSlots.contains(time))
                    .collect(Collectors.toList());

            // Format times for display
            List<String> formattedSlots = availableSlots.stream()
                    .map(time -> time.format(DateTimeFormatter.ofPattern("HH:mm")))
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("availableSlots", formattedSlots);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get available slots: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Helper method to generate time slots
    private List<LocalTime> generateTimeSlots(int startHour, int endHour, int minutesInterval) {
        List<LocalTime> slots = new java.util.ArrayList<>();
        LocalTime time = LocalTime.of(startHour, 0);
        LocalTime end = LocalTime.of(endHour, 0);

        while (time.isBefore(end) || time.equals(end)) {
            slots.add(time);
            time = time.plusMinutes(minutesInterval);
        }

        return slots;
    }
    @GetMapping("/doctor/day")
    public ResponseEntity<Map<String, Object>> getDoctorAppointmentsForDay(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Map<String, Object> response = new HashMap<>();
        try {
            Doctor doctor = doctorService.getDoctorByUsername(userDetails.getUsername());
            List<Appointment> appointments = appointmentService.getDoctorAppointmentsForDate(doctor, date);

            // Convert appointments to DTOs for proper JSON serialization
            List<AppointmentDTO> appointmentDTOs = appointments.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("appointments", appointmentDTOs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get appointments: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    @GetMapping("/patient/day")
    public ResponseEntity<Map<String, Object>> getPatientAppointmentsForDay(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Map<String, Object> response = new HashMap<>();
        try {
            Patient patient = patientService.getPatientByUsername(userDetails.getUsername());
            List<Appointment> appointments = appointmentService.getPatientAppointmentsForDate(patient, date);

            List<AppointmentDTO> appointmentDTOs = appointments.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("appointments", appointmentDTOs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get appointments: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    // Method for complete appointments

    @PostMapping("/{id}/complete")
    public ResponseEntity<Map<String, Object>> completeAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam(required = false) String notes) {

        Map<String, Object> response = new HashMap<>();
        try {
            Doctor doctor = doctorService.getDoctorByUsername(userDetails.getUsername());
            Optional<Appointment> appointmentOpt = appointmentService.getAppointmentById(id);

            if (!appointmentOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Appointment not found");
                return ResponseEntity.badRequest().body(response);
            }

            Appointment appointment = appointmentOpt.get();
            if (!appointment.getDoctor().getId().equals(doctor.getId())) {
                response.put("success", false);
                response.put("message", "You can only complete your own appointments");
                return ResponseEntity.badRequest().body(response);
            }

            // Update notes if provided
            if (notes != null && !notes.isEmpty()) {
                appointment.setNotes(notes);
                appointmentService.updateAppointment(appointment);
            }

            appointmentService.markAppointmentAsCompleted(id);

            response.put("success", true);
            response.put("message", "Appointment marked as completed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to complete appointment: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/completed")
    public ResponseEntity<Map<String, Object>> getCompletedAppointments(
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();
        try {
            String role = userDetails.getAuthorities().iterator().next().getAuthority();
            List<Appointment> completedAppointments;

            if (role.equals("ROLE_DOCTOR")) {
                Doctor doctor = doctorService.getDoctorByUsername(userDetails.getUsername());
                completedAppointments = appointmentService.getAppointmentsByDoctorAndStatus(
                        doctor, Appointment.AppointmentStatus.COMPLETED);
            } else {
                Patient patient = patientService.getPatientByUsername(userDetails.getUsername());
                completedAppointments = appointmentService.getAppointmentsByPatientAndStatus(
                        patient, Appointment.AppointmentStatus.COMPLETED);
            }

            // Convert to DTOs for JSON serialization
            List<AppointmentDTO> appointmentDTOs = completedAppointments.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("appointments", appointmentDTOs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get completed appointments: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

}