package com.example.healthsystem.controllers;

import com.example.healthsystem.model.Patient;
import com.example.healthsystem.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        Patient patient = patientService.getPatientByUsername(username);

        if (patient == null) {
            return "redirect:/login"; // Redirect to login if patient not found
        }

        // Add patient data to the model
        model.addAttribute("patient", patient);
        model.addAttribute("healthMetrics", patientService.getHealthMetrics(patient.getId()));
        model.addAttribute("trendAnalysis", patientService.getTrendAnalysis(patient.getId()));

        return "patient/dashboard";
    }
}