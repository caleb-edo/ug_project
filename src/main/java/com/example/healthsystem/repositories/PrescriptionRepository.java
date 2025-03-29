package com.example.healthsystem.repositories;

import com.example.healthsystem.model.Prescription;
import com.example.healthsystem.model.Patient;
import com.example.healthsystem.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByPatient(Patient patient);
    List<Prescription> findByPrescribingDoctor(Doctor doctor);
    List<Prescription> findByPatientAndStatus(Patient patient, Prescription.PrescriptionStatus status);
    List<Prescription> findByPrescribingDoctorAndStatus(Doctor doctor, Prescription.PrescriptionStatus status);
}