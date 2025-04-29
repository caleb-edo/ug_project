# ChronicCare: A web-based applcation for managing chronic patients' health

## Information about this repository

The repository should contain 52 unique files for a Spring Boot project and their directories:

==Model==

Appointment.java
Doctor.java
Message.java
Patient.java
Prescription.java
User.java

==Controllers==

AppointmentController.java
AuthController.java
DoctorController.java
MessageController.java
PatientController.java
PrescriptionController.java

==DTO==

AppointmentDTO.java
PatientStatistics.java
PatientUpdate.java

==Repositories==

AppointmentRepository.java
DoctorRepository.java
MessageRepository.java
PatientRepository.java
PrescriptionRepository.java
UserRepository.java

==Security==

CustomUserDetails.java
CustomUserDetailsService.java

==Service==

AppointmentService.java
DoctorService.java
MessageService.java
PatientService.java
PatientUpdateService.java
PrescriptionService.java
UserService.java

==Config==

SecurityConfig.java

==Spring Boot Application Build==

HealthSystemApplication.java

==Tests==

HealthSystemApplicationTests.java
HealthSystemControllerTests.java
HealthSystemSecurityTests.java
HealthSystemServiceTests.java

==System Files==

build.gradle
application.properties

==Database schema (optional)==
HealthSystem.sql

Registering new accounts should work for normal access, but sample doctor and patient logins are as follows:

==Doctor Login==
Username: bensmith
Password: bensmith123

==Patient Login==
Username: frankfelix
Password: frankfelix123

Any suitable IDE with Spring Boot capabilities (Intellij, Visual Studio Code, Eclipse, etc) are suitable to run the application. IntelliJ would be preferred. The application runs at https;//localhost:8080
