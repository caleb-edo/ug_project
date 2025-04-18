package com.example.healthsystem;

import com.example.healthsystem.model.*;
import com.example.healthsystem.repositories.*;
import com.example.healthsystem.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Main test class for the Health System application.
 * This class serves as a starting point for application testing and includes
 * some basic tests for core functionalities.
 */
@SpringBootTest
class HealthSystemApplicationTests {

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Service components for testing
    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PrescriptionService prescriptionService;

    // Repository mocks (to avoid database operations)
    @MockBean
    private DoctorRepository doctorRepository;

    @MockBean
    private PatientRepository patientRepository;

    @MockBean
    private AppointmentRepository appointmentRepository;

    @MockBean
    private PrescriptionRepository prescriptionRepository;

    @Test
    @DisplayName("Application context loads successfully")
    void contextLoads() {
        // This test verifies that the Spring context loads without errors
        assertNotNull(passwordEncoder);
        assertNotNull(doctorService);
        assertNotNull(patientService);
        assertNotNull(appointmentService);
        assertNotNull(prescriptionService);
    }

    @Test
    @DisplayName("Password encoder works correctly")
    void passwordEncoder_EncodesAndVerifiesPasswords() {
        // Test password encoding and verification
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
        assertFalse(passwordEncoder.matches("wrongpassword", encodedPassword));
    }

    @Test
    @DisplayName("Core service components function properly")
    void coreServiceComponents_FunctionProperly() {
        // Test doctor service
        Doctor doctor = new Doctor();
        doctor.setUsername("testdoctor");
        when(doctorRepository.findByUsername("testdoctor")).thenReturn(Optional.of(doctor));

        Doctor foundDoctor = doctorService.getDoctorByUsername("testdoctor");
        assertNotNull(foundDoctor);
        assertEquals("testdoctor", foundDoctor.getUsername());

        // Test patient service
        Patient patient = new Patient();
        patient.setUsername("testpatient");
        when(patientRepository.findByUsername("testpatient")).thenReturn(Optional.of(patient));

        Patient foundPatient = patientService.getPatientByUsername("testpatient");
        assertNotNull(foundPatient);
        assertEquals("testpatient", foundPatient.getUsername());

        // Verify the mocks were called
        verify(doctorRepository).findByUsername("testdoctor");
        verify(patientRepository).findByUsername("testpatient");
    }

    @Test
    @DisplayName("Patient health metrics are available")
    void patientHealthMetrics_AreAvailable() {
        // Test health metrics functionality
        var metrics = patientService.getHealthMetrics(1L);

        assertNotNull(metrics);
        assertFalse(metrics.isEmpty());
        assertTrue(metrics.containsKey("Blood Pressure"));
        assertEquals("120/80 mmHg", metrics.get("Blood Pressure"));
    }

    @Test
    @DisplayName("Patient trend analysis is available")
    void patientTrendAnalysis_IsAvailable() {
        // Test trend analysis functionality
        var trends = patientService.getTrendAnalysis(1L);

        assertNotNull(trends);
        assertFalse(trends.isEmpty());
        assertTrue(trends.containsKey("Blood Pressure Trend"));
        assertEquals("Stable", trends.get("Blood Pressure Trend"));
    }

    @Test
    @DisplayName("Model associations function correctly")
    void modelAssociations_FunctionCorrectly() {
        // Create test entities
        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setUsername("doctor1");
        doctor.setFullName("Dr. Test");

        Patient patient = new Patient();
        patient.setId(1L);
        patient.setUsername("patient1");
        patient.setFullName("Test Patient");

        // Test doctor-patient association
        patient.setDoctor(doctor);
        assertEquals(doctor, patient.getDoctor());
        assertEquals("Dr. Test", patient.getDoctor().getFullName());

        // Test prescription association
        Prescription prescription = new Prescription();
        prescription.setId(1L);
        prescription.setMedicationName("Test Medication");
        prescription.setPatient(patient);
        prescription.setPrescribingDoctor(doctor);

        assertEquals(patient, prescription.getPatient());
        assertEquals(doctor, prescription.getPrescribingDoctor());
        assertEquals("Test Patient", prescription.getPatient().getFullName());
        assertEquals("Dr. Test", prescription.getPrescribingDoctor().getFullName());
    }

    @Test
    @DisplayName("Entity inheritance works correctly")
    void entityInheritance_WorksCorrectly() {
        // Test inheritance from User class
        Doctor doctor = new Doctor();
        doctor.setUsername("doctor1");
        doctor.setPassword("password");
        doctor.setRole("DOCTOR");
        doctor.setFullName("Dr. Test");

        assertEquals("doctor1", doctor.getUsername());
        assertEquals("password", doctor.getPassword());
        assertEquals("DOCTOR", doctor.getRole());

        Patient patient = new Patient();
        patient.setUsername("patient1");
        patient.setPassword("password");
        patient.setRole("PATIENT");
        patient.setFullName("Test Patient");

        assertEquals("patient1", patient.getUsername());
        assertEquals("password", patient.getPassword());
        assertEquals("PATIENT", patient.getRole());

        // Test polymorphic behavior
        User doctorAsUser = doctor;
        User patientAsUser = patient;

        assertEquals("Dr. Test", doctorAsUser.getFullName());
        assertEquals("Test Patient", patientAsUser.getFullName());
    }
}