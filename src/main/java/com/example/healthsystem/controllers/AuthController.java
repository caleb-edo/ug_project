package com.example.healthsystem.controllers;

import com.example.healthsystem.model.User;
import com.example.healthsystem.model.Patient;
import com.example.healthsystem.repositories.UserRepository;
import com.example.healthsystem.repositories.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.text.SimpleDateFormat;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
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
                               @RequestParam(required = false) String currentMedications) {

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        User savedUser = userRepository.save(user);

        if ("PATIENT".equals(role)) {
            try {
                Patient patient = new Patient();
                patient.setId(savedUser.getId());
                patient.setUsername(savedUser.getUsername());
                patient.setPassword(savedUser.getPassword());
                patient.setRole(savedUser.getRole());

                patient.setFullName(fullName);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                patient.setDateOfBirth(dateFormat.parse(dateOfBirth));
                patient.setGender(gender);
                patient.setContactNumber(contactNumber);
                patient.setAddress(address);
                patient.setMedicalHistory(medicalHistory);
                patient.setAllergies(allergies);
                patient.setCurrentMedications(currentMedications);

                patientRepository.save(patient);
            } catch (Exception e) {
                userRepository.delete(savedUser);
                throw new RuntimeException("Failed to create patient profile", e);
            }
        }

        return "redirect:/login";
    }
}