package com.internshiptracker.controller;

import com.internshiptracker.entity.*;
import com.internshiptracker.repository.ApplicationRepository;
import com.internshiptracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        applicationRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setFullName("Dashboard User");
        testUser.setEmail("dash@test.com");
        testUser.setPasswordHash(passwordEncoder.encode("password123"));
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("Should return accurate dashboard summary and calculate success rate")
    void testDashboardSummary_CalculatesKPIs() throws Exception {
        // App 1: Offer Received
        Application app1 = new Application();
        app1.setUser(testUser);
        app1.setCompanyName("Company A");
        app1.setJobRole("Role A");
        app1.setApplicationDate(LocalDate.now().minusDays(10));
        app1.setStatus(ApplicationStatus.OFFER_RECEIVED);
        applicationRepository.save(app1);

        // App 2: Rejected
        Application app2 = new Application();
        app2.setUser(testUser);
        app2.setCompanyName("Company B");
        app2.setJobRole("Role B");
        app2.setApplicationDate(LocalDate.now().minusDays(5));
        app2.setStatus(ApplicationStatus.REJECTED);
        applicationRepository.save(app2);

        // App 3: Applied with deadline in 3 days
        Application app3 = new Application();
        app3.setUser(testUser);
        app3.setCompanyName("Company C");
        app3.setJobRole("Role C");
        app3.setApplicationDate(LocalDate.now().minusDays(2));
        app3.setDeadline(LocalDate.now().plusDays(3));
        app3.setStatus(ApplicationStatus.APPLIED);
        applicationRepository.save(app3);

        mockMvc.perform(get("/api/dashboard/summary")
                        .with(user("dash@test.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalApplications", is(3)))
                .andExpect(jsonPath("$.data.offersReceived", is(1)))
                .andExpect(jsonPath("$.data.rejectedApplications", is(1)))
                .andExpect(jsonPath("$.data.applicationsSubmitted", is(1)))
                .andExpect(jsonPath("$.data.upcomingDeadlines", is(1)))
                // 1 offer out of 2 resolved (1 offer + 1 reject) = 50.0%
                .andExpect(jsonPath("$.data.successRate", is(50.0)));
    }

    @Test
    @DisplayName("Should return status distribution list for all application statuses")
    void testStatusDistribution() throws Exception {
        Application app = new Application();
        app.setUser(testUser);
        app.setCompanyName("Distribution Corp");
        app.setJobRole("Data Intern");
        app.setApplicationDate(LocalDate.now());
        app.setStatus(ApplicationStatus.ONLINE_ASSESSMENT);
        applicationRepository.save(app);

        mockMvc.perform(get("/api/dashboard/status-distribution")
                        .with(user("dash@test.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(8))));
    }

    @Test
    @DisplayName("Should return upcoming and overdue deadlines")
    void testUpcomingDeadlines() throws Exception {
        Application overdue = new Application();
        overdue.setUser(testUser);
        overdue.setCompanyName("Late Corp");
        overdue.setJobRole("Late Role");
        overdue.setApplicationDate(LocalDate.now().minusDays(15));
        overdue.setDeadline(LocalDate.now().minusDays(2));
        overdue.setStatus(ApplicationStatus.APPLIED);
        applicationRepository.save(overdue);

        Application upcoming = new Application();
        upcoming.setUser(testUser);
        upcoming.setCompanyName("Soon Corp");
        upcoming.setJobRole("Soon Role");
        upcoming.setApplicationDate(LocalDate.now().minusDays(2));
        upcoming.setDeadline(LocalDate.now().plusDays(4));
        upcoming.setStatus(ApplicationStatus.APPLIED);
        applicationRepository.save(upcoming);

        mockMvc.perform(get("/api/dashboard/upcoming-deadlines")
                        .with(user("dash@test.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].overdue", is(true)))
                .andExpect(jsonPath("$.data[1].overdue", is(false)));
    }
}
