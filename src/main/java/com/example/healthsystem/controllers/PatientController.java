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
        try {
            String username = userDetails.getUsername();
            Patient patient = patientService.getPatientByUsername(username);

            model.addAttribute("patient", patient);
            model.addAttribute("healthMetrics", patientService.getHealthMetrics(patient.getId()));
            model.addAttribute("trendAnalysis", patientService.getTrendAnalysis(patient.getId()));

            return "patient-dashboard";
        } catch (Exception e) {
            return "redirect:/login?error";
        }
    }
}