package com.internshiptracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internshiptracker.dto.InterviewRequest;
import com.internshiptracker.entity.*;
import com.internshiptracker.repository.ApplicationRepository;
import com.internshiptracker.repository.InterviewRepository;
import com.internshiptracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InterviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Application testApp;

    @BeforeEach
    void setUp() {
        interviewRepository.deleteAll();
        applicationRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setFullName("Interview Candidate");
        testUser.setEmail("candidate@test.com");
        testUser.setPasswordHash(passwordEncoder.encode("password123"));
        testUser = userRepository.save(testUser);

        testApp = new Application();
        testApp.setUser(testUser);
        testApp.setCompanyName("Stripe");
        testApp.setJobRole("Software Engineer Intern");
        testApp.setApplicationDate(LocalDate.now());
        testApp.setStatus(ApplicationStatus.APPLIED);
        testApp = applicationRepository.save(testApp);
    }

    @Test
    @DisplayName("Should schedule an interview and update parent application status to Interview Scheduled")
    void testScheduleInterview_Success() throws Exception {
        InterviewRequest req = new InterviewRequest();
        req.setInterviewRound("Round 1 - Algorithms");
        req.setInterviewDate(LocalDateTime.now().plusDays(2));
        req.setInterviewMode(InterviewMode.ONLINE);
        req.setMeetingLink("https://meet.google.com/abc-defg-hij");
        req.setInterviewerName("Jane Smith");
        req.setPreparationNotes("Review Graphs and Dynamic Programming");
        req.setOutcome(InterviewOutcome.PENDING);

        mockMvc.perform(post("/api/applications/" + testApp.getId() + "/interviews")
                        .with(user("candidate@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.interviewRound", is("Round 1 - Algorithms")))
                .andExpect(jsonPath("$.data.interviewerName", is("Jane Smith")));

        // Verify parent application status transitioned to INTERVIEW_SCHEDULED
        Application refreshedApp = applicationRepository.findById(testApp.getId()).orElseThrow();
        assertEquals(ApplicationStatus.INTERVIEW_SCHEDULED, refreshedApp.getStatus());
    }

    @Test
    @DisplayName("Deleting parent application should cascade delete all associated interviews")
    void testCascadeDelete_ApplicationDeletesInterviews() throws Exception {
        Interview interview = new Interview();
        interview.setApplication(testApp);
        interview.setInterviewRound("System Design");
        interview.setInterviewDate(LocalDateTime.now().plusDays(3));
        interview.setInterviewMode(InterviewMode.ONLINE);
        interview.setOutcome(InterviewOutcome.PENDING);
        interviewRepository.save(interview);

        assertEquals(1, interviewRepository.count());

        // Delete application
        mockMvc.perform(delete("/api/applications/" + testApp.getId())
                        .with(user("candidate@test.com")))
                .andExpect(status().isOk());

        // Verify both application and interview are deleted
        assertEquals(0, applicationRepository.count());
        assertEquals(0, interviewRepository.count());
    }
}
