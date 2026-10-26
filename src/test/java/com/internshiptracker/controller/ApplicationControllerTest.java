package com.internshiptracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internshiptracker.dto.ApplicationRequest;
import com.internshiptracker.dto.ApplicationStatusUpdateRequest;
import com.internshiptracker.dto.RegisterRequest;
import com.internshiptracker.entity.*;
import com.internshiptracker.repository.ApplicationRepository;
import com.internshiptracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        applicationRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setFullName("Primary User");
        testUser.setEmail("primary@test.com");
        testUser.setPasswordHash(passwordEncoder.encode("password123"));
        testUser = userRepository.save(testUser);

        otherUser = new User();
        otherUser.setFullName("Other User");
        otherUser.setEmail("other@test.com");
        otherUser.setPasswordHash(passwordEncoder.encode("password123"));
        otherUser = userRepository.save(otherUser);
    }

    @Test
    @DisplayName("Should successfully create a new internship application")
    void testCreateApplication_Success() throws Exception {
        ApplicationRequest req = new ApplicationRequest();
        req.setCompanyName("Acme Corp");
        req.setJobRole("Software Engineer Intern");
        req.setWorkMode(WorkMode.REMOTE);
        req.setStatus(ApplicationStatus.APPLIED);
        req.setPriority(Priority.HIGH);
        req.setApplicationDate(LocalDate.now());
        req.setDeadline(LocalDate.now().plusDays(14));
        req.setStipend(new BigDecimal("5000.00"));
        req.setLocation("San Francisco, CA");

        mockMvc.perform(post("/api/applications")
                        .with(user("primary@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.companyName", is("Acme Corp")))
                .andExpect(jsonPath("$.data.jobRole", is("Software Engineer Intern")))
                .andExpect(jsonPath("$.data.workMode", is("Remote")))
                .andExpect(jsonPath("$.data.status", is("Applied")));
    }

    @Test
    @DisplayName("Should reject application creation if company name or role is blank")
    void testCreateApplication_MissingRequiredFields_Returns400() throws Exception {
        ApplicationRequest req = new ApplicationRequest();
        req.setCompanyName(""); // blank
        req.setJobRole("Developer");
        req.setApplicationDate(LocalDate.now());

        mockMvc.perform(post("/api/applications")
                        .with(user("primary@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.companyName", notNullValue()));
    }

    @Test
    @DisplayName("Should reject application if deadline precedes application date")
    void testCreateApplication_InvalidDates_Returns400() throws Exception {
        ApplicationRequest req = new ApplicationRequest();
        req.setCompanyName("Invalid Corp");
        req.setJobRole("Data Analyst Intern");
        req.setApplicationDate(LocalDate.now());
        req.setDeadline(LocalDate.now().minusDays(3)); // Before application date!

        mockMvc.perform(post("/api/applications")
                        .with(user("primary@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("cannot precede the application date")));
    }

    @Test
    @DisplayName("Should filter applications by search query and status")
    void testGetApplications_WithFilters() throws Exception {
        Application app1 = new Application();
        app1.setUser(testUser);
        app1.setCompanyName("Google");
        app1.setJobRole("Cloud Intern");
        app1.setApplicationDate(LocalDate.now());
        app1.setStatus(ApplicationStatus.APPLIED);
        app1.setWorkMode(WorkMode.REMOTE);
        app1.setPriority(Priority.HIGH);
        applicationRepository.save(app1);

        Application app2 = new Application();
        app2.setUser(testUser);
        app2.setCompanyName("Amazon");
        app2.setJobRole("Frontend Intern");
        app2.setApplicationDate(LocalDate.now());
        app2.setStatus(ApplicationStatus.OFFER_RECEIVED);
        app2.setWorkMode(WorkMode.HYBRID);
        app2.setPriority(Priority.MEDIUM);
        applicationRepository.save(app2);

        // Search for 'Google'
        mockMvc.perform(get("/api/applications?search=Google")
                        .with(user("primary@test.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].companyName", is("Google")));

        // Filter by status 'Offer Received'
        mockMvc.perform(get("/api/applications?status=OFFER_RECEIVED")
                        .with(user("primary@test.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].companyName", is("Amazon")));
    }

    @Test
    @DisplayName("Should update application status via PATCH endpoint")
    void testUpdateStatus_Success() throws Exception {
        Application app = new Application();
        app.setUser(testUser);
        app.setCompanyName("Netflix");
        app.setJobRole("Backend Intern");
        app.setApplicationDate(LocalDate.now());
        app.setStatus(ApplicationStatus.APPLIED);
        app = applicationRepository.save(app);

        ApplicationStatusUpdateRequest req = new ApplicationStatusUpdateRequest(ApplicationStatus.INTERVIEW_SCHEDULED);

        mockMvc.perform(patch("/api/applications/" + app.getId() + "/status")
                        .with(user("primary@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("Interview Scheduled")));
    }

    @Test
    @DisplayName("Security Isolation: User B cannot access User A's application")
    void testUserIsolation_CannotAccessAnotherUsersApplication() throws Exception {
        // Create application owned by testUser (primary@test.com)
        Application app = new Application();
        app.setUser(testUser);
        app.setCompanyName("Secret Company");
        app.setJobRole("Research Intern");
        app.setApplicationDate(LocalDate.now());
        app.setStatus(ApplicationStatus.APPLIED);
        app = applicationRepository.save(app);

        // otherUser (other@test.com) tries to read testUser's application
        mockMvc.perform(get("/api/applications/" + app.getId())
                        .with(user("other@test.com")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Application not found")));

        // otherUser tries to delete testUser's application
        mockMvc.perform(delete("/api/applications/" + app.getId())
                        .with(user("other@test.com")))
                .andExpect(status().isNotFound());
    }
}
