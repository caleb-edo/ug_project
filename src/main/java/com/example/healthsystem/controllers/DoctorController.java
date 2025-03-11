package com.example.healthsystem.controllers;

import com.example.healthsystem.model.Doctor;
import com.example.healthsystem.model.Message;
import com.example.healthsystem.model.Patient;
import com.example.healthsystem.service.DoctorService;
import com.example.healthsystem.service.MessageService;
import com.example.healthsystem.service.PatientService;
import com.example.healthsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            String username = userDetails.getUsername();
            Doctor doctor = doctorService.getDoctorByUsername(username);

            // Get assigned and unassigned patients
            List<Patient> unassignedPatients = patientService.getUnassignedPatients();
            List<Message> messages = messageService.getMessagesByReceiver(doctor);

            model.addAttribute("doctor", doctor);
            model.addAttribute("patients", doctor.getPatients());
            model.addAttribute("unassignedPatients", unassignedPatients);
            model.addAttribute("patientStats", doctorService.getPatientStatistics(doctor.getId()));
            model.addAttribute("recentUpdates", doctorService.getRecentPatientUpdates(doctor.getId()));
            model.addAttribute("messages", messages);

            return "doctor-dashboard";
        } catch (Exception e) {
            return "redirect:/login?error";
        }
    }

    @PostMapping("/assign-patient")
    public String assignPatient(@AuthenticationPrincipal UserDetails userDetails, @RequestParam Long patientId) {
        try {
            String username = userDetails.getUsername();
            Doctor doctor = doctorService.getDoctorByUsername(username);
            patientService.assignPatientToDoctor(patientId, doctor);

            return "redirect:/doctor/dashboard?success";
        } catch (Exception e) {
            return "redirect:/doctor/dashboard?error";
        }
    }

    @GetMapping("/patient/{patientId}")
    public String viewPatientDetails(@PathVariable Long patientId, Model model) {
        try {
            Patient patient = patientService.getPatientById(patientId);

            model.addAttribute("patient", patient);
            model.addAttribute("doctor", patient.getDoctor());
            model.addAttribute("healthMetrics", patientService.getHealthMetrics(patientId));
            model.addAttribute("trendAnalysis", patientService.getTrendAnalysis(patientId));

            return "patient-details";
        } catch (Exception e) {
            return "redirect:/doctor/dashboard?error";
        }
    }
}