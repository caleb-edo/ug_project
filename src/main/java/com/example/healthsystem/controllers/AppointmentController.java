package com.example.healthsystem.controllers;

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

            model.addAttribute("doctor", doctor);
            model.addAttribute("pendingAppointments", pendingAppointments);
            model.addAttribute("confirmedAppointments", confirmedAppointments);
            return "doctor-appointments";
        } else {
            Patient patient = patientService.getPatientByUsername(username);
            List<Appointment> pendingAppointments = appointmentService.getPendingAppointmentsByPatient(patient);
            List<Appointment> confirmedAppointments = appointmentService.getConfirmedAppointmentsByPatient(patient);

            model.addAttribute("patient", patient);
            model.addAttribute("pendingAppointments", pendingAppointments);
            model.addAttribute("confirmedAppointments", confirmedAppointments);
            return "patient-appointments";
        }
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
}