package com.example.healthsystem.controllers;

import com.example.healthsystem.model.Prescription;
import com.example.healthsystem.model.Doctor;
import com.example.healthsystem.model.Patient;
import com.example.healthsystem.service.PrescriptionService;
import com.example.healthsystem.service.DoctorService;
import com.example.healthsystem.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/prescriptions")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    // Patient view of prescriptions
    @GetMapping("/patient")
    public String patientPrescriptions(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            Patient patient = patientService.getPatientByUsername(userDetails.getUsername());
            List<Prescription> prescriptions = prescriptionService.getPrescriptionsForPatient(patient.getId());
            model.addAttribute("prescriptions", prescriptions);
            return "patient-prescriptions";
        } catch (Exception e) {
            return "redirect:/patient/dashboard?error";
        }
    }

    // Doctor view of prescriptions
    @GetMapping("/doctor")
    public String doctorPrescriptions(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            Doctor doctor = doctorService.getDoctorByUsername(userDetails.getUsername());
            List<Prescription> prescriptions = prescriptionService.getPrescriptionsForDoctor(doctor.getId());
            model.addAttribute("prescriptions", prescriptions);
            return "doctor-prescriptions";
        } catch (Exception e) {
            return "redirect:/doctor/dashboard?error";
        }
    }

    // Patient request for prescription refill
    @PostMapping("/patient/request-refill")
    public String requestPrescriptionRefill(@AuthenticationPrincipal UserDetails userDetails,
                                            @RequestParam Long prescriptionId) {
        try {
            Patient patient = patientService.getPatientByUsername(userDetails.getUsername());
            prescriptionService.requestRefill(prescriptionId, patient.getId());
            return "redirect:/prescriptions/patient?refillRequested";
        } catch (Exception e) {
            return "redirect:/prescriptions/patient?error";
        }
    }

    // Doctor view of pending refill requests
    @GetMapping("/doctor/refill-requests")
    public String refillRequests(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            Doctor doctor = doctorService.getDoctorByUsername(userDetails.getUsername());
            List<Prescription> pendingRefills = prescriptionService.getPendingRefillsForDoctor(doctor.getId());
            model.addAttribute("pendingRefills", pendingRefills);
            return "doctor-refill-requests";
        } catch (Exception e) {
            return "redirect:/doctor/dashboard?error";
        }
    }

    // Doctor approve/deny refill request
    @PostMapping("/doctor/process-refill")
    public String processRefillRequest(@AuthenticationPrincipal UserDetails userDetails,
                                       @RequestParam Long prescriptionId,
                                       @RequestParam boolean approve) {
        try {
            prescriptionService.processRefillRequest(prescriptionId, approve);
            return "redirect:/prescriptions/doctor/refill-requests?success";
        } catch (Exception e) {
            return "redirect:/prescriptions/doctor/refill-requests?error";
        }
    }

    // Create new prescription (for doctors)
    @GetMapping("/doctor/new")
    public String newPrescriptionForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            Doctor doctor = doctorService.getDoctorByUsername(userDetails.getUsername());
            // Only get patients assigned to this doctor instead of all patients
            List<Patient> patients = doctor.getPatients();
            model.addAttribute("doctor", doctor);
            model.addAttribute("patients", patients);
            return "doctor-new-prescription";
        } catch (Exception e) {
            return "redirect:/doctor/dashboard?error";
        }
    }

    // Submit new prescription
    @PostMapping("/doctor/new")
    public String createPrescription(@AuthenticationPrincipal UserDetails userDetails,
                                     @ModelAttribute Prescription prescription,
                                     @RequestParam Long patientId) {
        try {
            Doctor doctor = doctorService.getDoctorByUsername(userDetails.getUsername());
            prescriptionService.createPrescription(patientId, doctor.getId(), prescription);
            return "redirect:/prescriptions/doctor?success";
        } catch (Exception e) {
            return "redirect:/prescriptions/doctor/new?error";
        }
    }
}