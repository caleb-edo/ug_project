package com.example.healthsystem.controllers;

import com.example.healthsystem.model.Doctor;
import com.example.healthsystem.model.Message;
import com.example.healthsystem.model.Patient;
import com.example.healthsystem.model.User;
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
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientService patientService;

    @GetMapping
    public String viewMessages(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User currentUser = userService.getUserByUsername(username);

        List<Message> receivedMessages = messageService.getMessagesByReceiver(currentUser);
        List<Message> sentMessages = messageService.getMessagesBySender(currentUser);


        model.addAttribute("user", currentUser);
        model.addAttribute("receivedMessages", receivedMessages);
        model.addAttribute("sentMessages", sentMessages);

        return "messages";
    }

    @GetMapping("/compose")
    public String composeMessage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        User sender = userService.getUserByUsername(username);

        List<Doctor> doctors = doctorService.getAllDoctors();
        List<Patient> patients = patientService.getAllPatients();

        model.addAttribute("message", new Message());
        model.addAttribute("doctors", doctors);
        model.addAttribute("patients", patients);
        model.addAttribute("sender", sender);

        return "compose-message";
    }

    @PostMapping("/send")
    public String sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long receiverId,
            @RequestParam String content) {
        try {
            String username = userDetails.getUsername();
            User sender = userService.getUserByUsername(username);
            User receiver = userService.getUserById(receiverId);

            messageService.sendMessage(sender, receiver, content);

            return "redirect:/messages?success";
        } catch (Exception e) {
            return "redirect:/messages?error";
        }
    }
}
