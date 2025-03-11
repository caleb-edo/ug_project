package com.example.healthsystem.controllers;

import com.example.healthsystem.model.Message;
import com.example.healthsystem.model.Patient;
import com.example.healthsystem.service.MessageService;
import com.example.healthsystem.service.PatientService;
import com.example.healthsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/patient")
public class PatientController {

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
            Patient patient = patientService.getPatientByUsername(username);

            List<Message> messages = messageService.getMessagesByReceiver(patient);

            model.addAttribute("patient", patient);
            model.addAttribute("healthMetrics", patientService.getHealthMetrics(patient.getId()));
            model.addAttribute("trendAnalysis", patientService.getTrendAnalysis(patient.getId()));
            model.addAttribute("messages", messages);

            return "patient-dashboard";
        } catch (Exception e) {
            return "redirect:/login?error";
        }
    }
}