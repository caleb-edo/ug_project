package com.example.healthsystem;

import com.example.healthsystem.security.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for security configurations in the Health System.
 * Reformatted to match the actual security implementation.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class HealthSystemSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Public endpoints should be accessible without authentication")
    void publicEndpoints_ShouldBeAccessibleWithoutAuthentication() throws Exception {
        // Login page should be accessible
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());

        // Register page should be accessible
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("Protected endpoints should redirect unauthenticated users to login")
    void protectedEndpoints_ShouldRedirectUnauthenticatedUsersToLogin() throws Exception {
        // Doctor dashboard should redirect to login
        mockMvc.perform(get("/doctor/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        // Patient dashboard should redirect to login
        mockMvc.perform(get("/patient/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    @DisplayName("Patient should not have access to doctor endpoints")
    void patient_ShouldNotHaveAccessToDoctorEndpoints() throws Exception {
        // When a patient tries to access doctor dashboard, should get forbidden
        mockMvc.perform(get("/doctor/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("Doctor should not have access to patient endpoints")
    void doctor_ShouldNotHaveAccessToPatientEndpoints() throws Exception {
        // When a doctor tries to access patient dashboard, should get forbidden
        mockMvc.perform(get("/patient/dashboard"))
                .andExpect(status().isForbidden());
    }

}