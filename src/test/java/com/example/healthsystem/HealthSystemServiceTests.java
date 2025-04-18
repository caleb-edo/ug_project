package com.example.healthsystem;

import com.example.healthsystem.model.*;
import com.example.healthsystem.repositories.*;
import com.example.healthsystem.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Consolidated test class for service layer components of the Health System.
 * Uses nested test classes to organize tests by service.
 */
@ExtendWith(MockitoExtension.class)
public class HealthSystemServiceTests {

    @Nested
    @DisplayName("PatientService Tests")
    class PatientServiceTests {
        @Mock
        private PatientRepository patientRepository;

        @InjectMocks
        private PatientService patientService;

        @Test
        @DisplayName("Get patient by username returns correct patient")
        void getPatientByUsername_ReturnsCorrectPatient() {
            // Arrange
            String username = "patient1";
            Patient expectedPatient = new Patient();
            expectedPatient.setUsername(username);
            expectedPatient.setFullName("Test Patient");

            when(patientRepository.findByUsername(username)).thenReturn(Optional.of(expectedPatient));

            // Act
            Patient result = patientService.getPatientByUsername(username);

            // Assert
            assertEquals(username, result.getUsername());
            assertEquals("Test Patient", result.getFullName());
        }

        @Test
        @DisplayName("Get health metrics returns non-empty map")
        void getHealthMetrics_ReturnsNonEmptyMap() {
            // Act
            Map<String, String> metrics = patientService.getHealthMetrics(1L);

            // Assert
            assertNotNull(metrics);
            assertFalse(metrics.isEmpty());
            assertTrue(metrics.containsKey("Blood Pressure"));
        }
    }

    @Nested
    @DisplayName("DoctorService Tests")
    @ExtendWith(MockitoExtension.class)
    class DoctorServiceTests {
        @Mock
        private DoctorRepository doctorRepository;

        @Mock
        private PatientRepository patientRepository;

        @Mock
        private AppointmentService appointmentService;

        @InjectMocks
        private DoctorService doctorService;

        @Test
        @DisplayName("Get doctor by username returns correct doctor")
        void getDoctorByUsername_ReturnsCorrectDoctor() {
            // Arrange
            String username = "doctor1";
            Doctor expectedDoctor = new Doctor();
            expectedDoctor.setUsername(username);
            expectedDoctor.setFullName("Dr. Test");
            expectedDoctor.setSpecialization("General");

            when(doctorRepository.findByUsername(username)).thenReturn(Optional.of(expectedDoctor));

            // Act
            Doctor result = doctorService.getDoctorByUsername(username);

            // Assert
            assertEquals(username, result.getUsername());
            assertEquals("Dr. Test", result.getFullName());
            assertEquals("General", result.getSpecialization());
        }

        @Test
        @DisplayName("Get patient statistics returns valid statistics")
        void getPatientStatistics_ReturnsValidStatistics() {
            // Arrange
            Doctor doctor = new Doctor();
            doctor.setId(1L);
            doctor.setPatients(new ArrayList<>());

            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
            when(appointmentService.getPendingAppointmentsByDoctor(any())).thenReturn(Collections.emptyList());

            // Act
            var result = doctorService.getPatientStatistics(1L);

            // Assert
            assertNotNull(result);
            assertEquals(0, result.getTotalPatients());
        }
    }

    @Nested
    @DisplayName("AppointmentService Tests")
    @ExtendWith(MockitoExtension.class)
    class AppointmentServiceTests {
        @Mock
        private AppointmentRepository appointmentRepository;

        @InjectMocks
        private AppointmentService appointmentService;

        @Test
        @DisplayName("Create appointment sets correct status")
        void createAppointment_SetsCorrectStatus() {
            // Arrange
            Appointment appointment = new Appointment();
            appointment.setId(1L);
            appointment.setPatient(new Patient());
            appointment.setDoctor(new Doctor());
            appointment.setAppointmentDateTime(LocalDateTime.now().plusDays(1));

            when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            Appointment result = appointmentService.createAppointment(appointment);

            // Assert
            assertEquals(Appointment.AppointmentStatus.REQUESTED, result.getStatus());
        }

        @Test
        @DisplayName("Confirm appointment updates status")
        void confirmAppointment_UpdatesStatus() {
            // Arrange
            Appointment appointment = new Appointment();
            appointment.setId(1L);
            appointment.setStatus(Appointment.AppointmentStatus.REQUESTED);

            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            appointmentService.confirmAppointment(1L);

            // Assert
            assertEquals(Appointment.AppointmentStatus.CONFIRMED, appointment.getStatus());
        }
    }

    @Nested
    @DisplayName("PrescriptionService Tests")
    @ExtendWith(MockitoExtension.class)
    class PrescriptionServiceTests {
        @Mock
        private PrescriptionRepository prescriptionRepository;

        @Mock
        private PatientService patientService;

        @Mock
        private DoctorService doctorService;

        @InjectMocks
        private PrescriptionService prescriptionService;

        @Test
        @DisplayName("Create prescription sets correct status")
        void createPrescription_SetsCorrectStatus() {
            // Arrange
            Patient patient = new Patient();
            patient.setId(1L);
            Doctor doctor = new Doctor();
            doctor.setId(1L);
            Prescription prescription = new Prescription();
            prescription.setMedicationName("Test Medication");
            prescription.setDosage("10mg");
            prescription.setFrequency("Once daily");

            when(patientService.getPatientById(1L)).thenReturn(patient);
            when(doctorService.getDoctorById(1L)).thenReturn(doctor);
            when(prescriptionRepository.save(any(Prescription.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            Prescription result = prescriptionService.createPrescription(1L, 1L, prescription);

            // Assert
            assertEquals(Prescription.PrescriptionStatus.ACTIVE, result.getStatus());
            assertEquals(patient, result.getPatient());
            assertEquals(doctor, result.getPrescribingDoctor());
            assertNotNull(result.getPrescriptionDate());
            assertNotNull(result.getExpirationDate());
        }
    }

    @Nested
    @DisplayName("MessageService Tests")
    @ExtendWith(MockitoExtension.class)
    class MessageServiceTests {
        @Mock
        private MessageRepository messageRepository;

        @InjectMocks
        private MessageService messageService;

        @Test
        @DisplayName("Send message creates message with timestamp")
        void sendMessage_CreatesMessageWithTimestamp() {
            // Arrange
            User sender = new User();
            sender.setId(1L);
            User receiver = new User();
            receiver.setId(2L);
            String content = "Test message";

            when(messageRepository.save(any(Message.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            Message result = messageService.sendMessage(sender, receiver, content);

            // Assert
            assertEquals(sender, result.getSender());
            assertEquals(receiver, result.getReceiver());
            assertEquals(content, result.getContent());
            assertNotNull(result.getTimestamp());
        }
    }
}