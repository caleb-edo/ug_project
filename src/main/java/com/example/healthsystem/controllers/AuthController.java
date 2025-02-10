package com.example.healthsystem.controllers;

import com.example.healthsystem.model.Doctor;
import com.example.healthsystem.model.User;
import com.example.healthsystem.model.Patient;
import com.example.healthsystem.repositories.DoctorRepository;
import com.example.healthsystem.repositories.PatientRepository;
import com.example.healthsystem.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.SimpleDateFormat;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        return "redirect:/patient/dashboard";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "registered", required = false) String registered,
                        Model model,
                        @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails != null) {
            return "redirect:/";
        }

        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        if (registered != null) {
            model.addAttribute("message", "Registration successful! Please login.");
        }

        return "login";
    }

    @GetMapping("/register")
    public String register(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            return "redirect:/";
        }
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String role,
                               @RequestParam(required = false) String fullName,
                               @RequestParam(required = false) String dateOfBirth,
                               @RequestParam(required = false) String gender,
                               @RequestParam(required = false) String contactNumber,
                               @RequestParam(required = false) String address,
                               @RequestParam(required = false) String medicalHistory,
                               @RequestParam(required = false) String allergies,
                               @RequestParam(required = false) String currentMedications,
                               @RequestParam(required = false) String specialization,
                               @RequestParam(required = false) String licenseNumber,
                               RedirectAttributes redirectAttributes) {

        try {
            if (userRepository.findByUsername(username).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Username already exists");
                return "redirect:/register";
            }

            if ("PATIENT".equals(role)) {
                Patient patient = new Patient();
                patient.setUsername(username);
                patient.setPassword(passwordEncoder.encode(password));
                patient.setRole(role);
                patient.setFullName(fullName);

                if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    patient.setDateOfBirth(dateFormat.parse(dateOfBirth));
                }

                patient.setGender(gender);
                patient.setContactNumber(contactNumber);
                patient.setAddress(address);
                patient.setMedicalHistory(medicalHistory);
                patient.setAllergies(allergies);
                patient.setCurrentMedications(currentMedications);

                patientRepository.save(patient);
            } else if ("DOCTOR".equals(role)) {
                Doctor doctor = new Doctor();
                doctor.setUsername(username);
                doctor.setPassword(passwordEncoder.encode(password));
                doctor.setRole(role);
                doctor.setFullName(fullName);
                doctor.setSpecialization(specialization);
                doctor.setLicenseNumber(licenseNumber);
                doctor.setContactNumber(contactNumber);
                doctor.setAddress(address);

                doctorRepository.save(doctor);
            } else {
                User user = new User();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode(password));
                user.setRole(role);
                userRepository.save(user);
            }

            redirectAttributes.addFlashAttribute("message", "Registration successful! Please login.");
            return "redirect:/login?registered";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Registration failed. Please try again.");
            return "redirect:/register";
        }
    }
}
