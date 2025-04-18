package com.example.healthsystem;

import com.example.healthsystem.config.SecurityConfig;
import com.example.healthsystem.controllers.AuthController;
import com.example.healthsystem.controllers.DoctorController;
import com.example.healthsystem.controllers.PatientController;
import com.example.healthsystem.dto.PatientStatistics;
import com.example.healthsystem.model.Doctor;
import com.example.healthsystem.model.Patient;
import com.example.healthsystem.repositories.DoctorRepository;
import com.example.healthsystem.repositories.PatientRepository;
import com.example.healthsystem.repositories.UserRepository;
import com.example.healthsystem.security.CustomUserDetailsService;
import com.example.healthsystem.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for the Health System application.
 */
public class HealthSystemControllerTests {

    /**
     * Tests for the AuthController
     */
    @WebMvcTest(AuthController.class)
    @Import(SecurityConfig.class)
    public static class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private UserRepository userRepository;

        @MockBean
        private PatientRepository patientRepository;

        @MockBean
        private DoctorRepository doctorRepository;

        @MockBean
        private PasswordEncoder passwordEncoder;

        @MockBean
        private CustomUserDetailsService customUserDetailsService;

        @Test
        @DisplayName("Login page should be accessible to everyone")
        public void loginPageShouldBeAccessible() throws Exception {
            mockMvc.perform(get("/login"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Register page should be accessible to everyone")
        public void registerPageShouldBeAccessible() throws Exception {
            mockMvc.perform(get("/register"))
                    .andExpect(status().isOk());
        }
    }

    /**
     * Tests for the DoctorController
     */
    @WebMvcTest(DoctorController.class)
    @Import(SecurityConfig.class)
    public static class DoctorControllerTest {
        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private DoctorService doctorService;

        @MockBean
        private PatientService patientService;

        @MockBean
        private MessageService messageService;

        @MockBean
        private PatientUpdateService patientUpdateService;

        @MockBean
        private AppointmentService appointmentService;

        @MockBean
        private CustomUserDetailsService customUserDetailsService;

        @Test
        @WithMockUser(username = "doctor", roles = {"DOCTOR"})
        @DisplayName("Doctor dashboard should return correct view and model")
        public void dashboard_ReturnsCorrectViewAndModel() throws Exception {
            // Arrange
            Doctor doctor = new Doctor();
            doctor.setFullName("Dr. Test");
            doctor.setPatients(new ArrayList<>());
            PatientStatistics stats = new PatientStatistics();

            when(doctorService.getDoctorByUsername("doctor")).thenReturn(doctor);
            when(patientService.getUnassignedPatients()).thenReturn(new ArrayList<>());
            when(messageService.getMessagesByReceiver(any())).thenReturn(new ArrayList<>());
            when(appointmentService.getPendingAppointmentsByDoctor(any())).thenReturn(new ArrayList<>());
            when(doctorService.getPatientStatistics(any())).thenReturn(stats);
            when(patientUpdateService.getRecentUpdatesForDoctor(any())).thenReturn(new ArrayList<>());

            // Act & Assert
            mockMvc.perform(get("/doctor/dashboard"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("doctor-dashboard"))
                    .andExpect(model().attributeExists("doctor", "patients", "unassignedPatients",
                            "patientStats", "recentUpdates", "messages", "pendingAppointments"));
        }

        @Test
        @WithMockUser(username = "doctor", roles = {"DOCTOR"})
        @DisplayName("Doctor should be able to view patient details")
        public void viewPatientDetails_ReturnsCorrectViewAndModel() throws Exception {
            // Arrange
            Patient patient = new Patient();
            patient.setFullName("Test Patient");
            Doctor doctor = new Doctor();
            doctor.setFullName("Dr. Test");
            patient.setDoctor(doctor);
            Map<String, String> metrics = new HashMap<>();
            Map<String, String> trends = new HashMap<>();

            when(patientService.getPatientById(1L)).thenReturn(patient);
            when(patientService.getHealthMetrics(1L)).thenReturn(metrics);
            when(patientService.getTrendAnalysis(1L)).thenReturn(trends);

            // Act & Assert
            mockMvc.perform(get("/doctor/patient/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("patient-details"))
                    .andExpect(model().attributeExists("patient", "doctor", "healthMetrics", "trendAnalysis"));
        }
    }

    /**
     * Tests for the PatientController
     */
    @WebMvcTest(PatientController.class)
    @Import(SecurityConfig.class)
    public static class PatientControllerTest {
        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private PatientService patientService;

        @MockBean
        private MessageService messageService;

        @MockBean
        private UserService userService;

        @MockBean
        private CustomUserDetailsService customUserDetailsService;

        @Test
        @WithMockUser(username = "patient", roles = {"PATIENT"})
        @DisplayName("Patient dashboard should return correct view and model")
        public void dashboard_ReturnsCorrectViewAndModel() throws Exception {
            // Arrange
            Patient patient = new Patient();
            patient.setId(1L);
            patient.setFullName("Test Patient");

            Map<String, String> metrics = new HashMap<>();
            metrics.put("Blood Pressure", "120/80 mmHg");

            Map<String, String> trends = new HashMap<>();
            trends.put("Blood Pressure Trend", "Stable");

            when(patientService.getPatientByUsername("patient")).thenReturn(patient);
            when(patientService.getHealthMetrics(1L)).thenReturn(metrics);
            when(patientService.getTrendAnalysis(1L)).thenReturn(trends);
            when(messageService.getMessagesByReceiver(any())).thenReturn(new ArrayList<>());

            // Act & Assert
            mockMvc.perform(get("/patient/dashboard"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("patient-dashboard"))
                    .andExpect(model().attributeExists("patient", "healthMetrics", "trendAnalysis", "messages"));
        }
    }
}