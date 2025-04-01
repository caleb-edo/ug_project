package com.example.healthsystem.service;

import com.example.healthsystem.model.Appointment;
import com.example.healthsystem.model.Doctor;
import com.example.healthsystem.model.Patient;
import com.example.healthsystem.repositories.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    public Appointment createAppointment(Appointment appointment) {
        // Set initial status to REQUESTED for new appointments
        appointment.setStatus(Appointment.AppointmentStatus.REQUESTED);
        return appointmentRepository.save(appointment);
    }

    public Appointment updateAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    public List<Appointment> getAppointmentsByPatient(Patient patient) {
        return appointmentRepository.findByPatient(patient);
    }

    public List<Appointment> getAppointmentsByDoctor(Doctor doctor) {
        return appointmentRepository.findByDoctor(doctor);
    }

    public List<Appointment> getPendingAppointmentsByDoctor(Doctor doctor) {
        return appointmentRepository.findByDoctorAndStatus(doctor, Appointment.AppointmentStatus.REQUESTED);
    }

    public List<Appointment> getConfirmedAppointmentsByDoctor(Doctor doctor) {
        return appointmentRepository.findByDoctorAndStatus(doctor, Appointment.AppointmentStatus.CONFIRMED);
    }

    public List<Appointment> getPendingAppointmentsByPatient(Patient patient) {
        return appointmentRepository.findByPatientAndStatus(patient, Appointment.AppointmentStatus.REQUESTED);
    }

    public List<Appointment> getConfirmedAppointmentsByPatient(Patient patient) {
        return appointmentRepository.findByPatientAndStatus(patient, Appointment.AppointmentStatus.CONFIRMED);
    }
    public List<Appointment> getAppointmentsByStatus(Appointment.AppointmentStatus status) {
        return appointmentRepository.findByStatus(status);
    }

    public List<Appointment> getRescheduledAppointmentsByDoctor(Doctor doctor) {
        return appointmentRepository.findByDoctorAndStatus(doctor, Appointment.AppointmentStatus.RESCHEDULED);
    }

    public List<Appointment> getRescheduledAppointmentsByPatient(Patient patient) {
        return appointmentRepository.findByPatientAndStatus(patient, Appointment.AppointmentStatus.RESCHEDULED);
    }
    public List<Appointment> getAppointmentsByDoctorAndStatus(Doctor doctor, Appointment.AppointmentStatus status) {
        return appointmentRepository.findByDoctorAndStatus(doctor, status);
    }

    public List<Appointment> getAppointmentsByPatientAndStatus(Patient patient, Appointment.AppointmentStatus status) {
        return appointmentRepository.findByPatientAndStatus(patient, status);
    }

    public List<Appointment> getDoctorAppointmentsForDate(Doctor doctor, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return appointmentRepository.findByDoctorAndAppointmentDateTimeBetween(doctor, startOfDay, endOfDay);
    }

    public List<Appointment> getPatientAppointmentsForDate(Patient patient, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return appointmentRepository.findByPatientAndAppointmentDateTimeBetween(patient, startOfDay, endOfDay);
    }

    public void confirmAppointment(Long appointmentId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
            appointmentRepository.save(appointment);
        }
    }

    public void rescheduleAppointment(Long appointmentId, LocalDateTime newDateTime) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            appointment.setAppointmentDateTime(newDateTime);
            appointment.setStatus(Appointment.AppointmentStatus.REQUESTED);
            appointmentRepository.save(appointment);
        }
    }

    public void cancelAppointment(Long appointmentId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
            appointmentRepository.save(appointment);
        }
    }

    public void markAppointmentAsCompleted(Long appointmentId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
            appointmentRepository.save(appointment);
        }
    }
}