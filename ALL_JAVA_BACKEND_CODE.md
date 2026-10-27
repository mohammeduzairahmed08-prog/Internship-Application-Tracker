# Complete Backend Java Source Code
> Project: Internship Application Tracker
> Total Java Source Files: 44

---

## Table of Contents

- src/main/java/com/internshiptracker/config/CustomUserDetailsService.java
- src/main/java/com/internshiptracker/config/DataInitializer.java
- src/main/java/com/internshiptracker/config/SecurityConfig.java
- src/main/java/com/internshiptracker/config/UserPrincipal.java
- src/main/java/com/internshiptracker/controller/ApplicationController.java
- src/main/java/com/internshiptracker/controller/AuthController.java
- src/main/java/com/internshiptracker/controller/DashboardController.java
- src/main/java/com/internshiptracker/controller/InterviewController.java
- src/main/java/com/internshiptracker/dto/ApiResponse.java
- src/main/java/com/internshiptracker/dto/ApplicationRequest.java
- src/main/java/com/internshiptracker/dto/ApplicationResponse.java
- src/main/java/com/internshiptracker/dto/ApplicationStatusUpdateRequest.java
- src/main/java/com/internshiptracker/dto/ApplicationTrendDto.java
- src/main/java/com/internshiptracker/dto/DashboardSummaryResponse.java
- src/main/java/com/internshiptracker/dto/ErrorResponse.java
- src/main/java/com/internshiptracker/dto/InterviewRequest.java
- src/main/java/com/internshiptracker/dto/InterviewResponse.java
- src/main/java/com/internshiptracker/dto/LoginRequest.java
- src/main/java/com/internshiptracker/dto/RegisterRequest.java
- src/main/java/com/internshiptracker/dto/StatusDistributionDto.java
- src/main/java/com/internshiptracker/dto/UpcomingDeadlineDto.java
- src/main/java/com/internshiptracker/dto/UpcomingInterviewDto.java
- src/main/java/com/internshiptracker/dto/UserResponse.java
- src/main/java/com/internshiptracker/entity/Application.java
- src/main/java/com/internshiptracker/entity/ApplicationStatus.java
- src/main/java/com/internshiptracker/entity/Interview.java
- src/main/java/com/internshiptracker/entity/InterviewMode.java
- src/main/java/com/internshiptracker/entity/InterviewOutcome.java
- src/main/java/com/internshiptracker/entity/Priority.java
- src/main/java/com/internshiptracker/entity/User.java
- src/main/java/com/internshiptracker/entity/WorkMode.java
- src/main/java/com/internshiptracker/exception/BadRequestException.java
- src/main/java/com/internshiptracker/exception/GlobalExceptionHandler.java
- src/main/java/com/internshiptracker/exception/ResourceNotFoundException.java
- src/main/java/com/internshiptracker/exception/UnauthorizedException.java
- src/main/java/com/internshiptracker/InternshipTrackerApplication.java
- src/main/java/com/internshiptracker/repository/ApplicationRepository.java
- src/main/java/com/internshiptracker/repository/ApplicationSpecification.java
- src/main/java/com/internshiptracker/repository/InterviewRepository.java
- src/main/java/com/internshiptracker/repository/UserRepository.java
- src/main/java/com/internshiptracker/service/ApplicationService.java
- src/main/java/com/internshiptracker/service/AuthService.java
- src/main/java/com/internshiptracker/service/DashboardService.java
- src/main/java/com/internshiptracker/service/InterviewService.java

---

## CustomUserDetailsService.java
**File Location**: src/main/java/com/internshiptracker/config/CustomUserDetailsService.java

```java
package com.internshiptracker.config;

import com.internshiptracker.entity.User;
import com.internshiptracker.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return UserPrincipal.create(user);
    }
}

```

---

## DataInitializer.java
**File Location**: src/main/java/com/internshiptracker/config/DataInitializer.java

```java
package com.internshiptracker.config;

import com.internshiptracker.entity.*;
import com.internshiptracker.repository.ApplicationRepository;
import com.internshiptracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ApplicationRepository applicationRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking database mock data initialization...");

        // 1. Initialize demo user: demo@internshiptracker.com
        User demoUser = userRepository.findByEmail("demo@internshiptracker.com")
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setFullName("Alex Developer");
                    newUser.setEmail("demo@internshiptracker.com");
                    newUser.setPasswordHash(passwordEncoder.encode("DemoPassword123!"));
                    return userRepository.save(newUser);
                });

        if (applicationRepository.countByUser(demoUser) == 0) {
            log.info("Seeding realistic mock applications for demo@internshiptracker.com...");
            seedApplicationsForUser(demoUser);
        }

        // 2. Initialize or seed user: democandidate@example.com
        userRepository.findByEmail("democandidate@example.com").ifPresent(user -> {
            if (applicationRepository.countByUser(user) == 0) {
                log.info("Seeding realistic mock applications for democandidate@example.com...");
                seedApplicationsForUser(user);
            }
        });

        log.info("Mock data initialization completed successfully.");
    }

    private void seedApplicationsForUser(User user) {
        LocalDate today = LocalDate.now();
        List<Application> applications = new ArrayList<>();

        // 1. Google - Interview Scheduled (with upcoming technical interview & approaching deadline)
        Application google = new Application();
        google.setUser(user);
        google.setCompanyName("Google");
        google.setJobRole("Software Engineering Intern - Cloud Infrastructure");
        google.setJobDescription("Develop high-throughput, distributed microservices supporting Google Cloud Storage and Compute Engine.");
        google.setCompanyWebsite("https://about.google");
        google.setApplicationUrl("https://careers.google.com/jobs/results/swe-intern");
        google.setLocation("Sunnyvale, CA");
        google.setWorkMode(WorkMode.HYBRID);
        google.setEmploymentType("INTERNSHIP");
        google.setApplicationDate(today.minusDays(14));
        google.setDeadline(today.plusDays(4));
        google.setStipend(new BigDecimal("9500.00"));
        google.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
        google.setPriority(Priority.HIGH);
        google.setNotes("Passed online coding assessment with 100%. Next round is technical live coding.");

        Interview googleOA = new Interview();
        googleOA.setInterviewRound("Round 1: Online Coding Assessment");
        googleOA.setInterviewDate(LocalDateTime.now().minusDays(7).withHour(14).withMinute(0));
        googleOA.setInterviewMode(InterviewMode.ONLINE);
        googleOA.setMeetingLink("https://hackerrank.com/eval/google-oa-8284");
        googleOA.setInterviewerName("Automated Platform");
        googleOA.setPreparationNotes("Solved 2 LeetCode Medium/Hard DP and Graph problems.");
        googleOA.setOutcome(InterviewOutcome.PASSED);
        google.addInterview(googleOA);

        Interview googleTech = new Interview();
        googleTech.setInterviewRound("Round 2: Technical Live Coding");
        googleTech.setInterviewDate(LocalDateTime.now().plusDays(2).withHour(15).withMinute(30));
        googleTech.setInterviewMode(InterviewMode.ONLINE);
        googleTech.setMeetingLink("https://meet.google.com/xyz-tracker-demo");
        googleTech.setInterviewerName("Sarah Jenkins (Staff SWE)");
        googleTech.setPreparationNotes("Focus on Binary Search, Trie trees, and concurrency principles.");
        googleTech.setOutcome(InterviewOutcome.PENDING);
        google.addInterview(googleTech);
        applications.add(google);

        // 2. Microsoft - Offer Received
        Application msft = new Application();
        msft.setUser(user);
        msft.setCompanyName("Microsoft");
        msft.setJobRole("SWE Intern - Azure Kubernetes Service");
        msft.setJobDescription("Work with the AKS platform team building automated failover and telemetry extensions for container runtimes.");
        msft.setCompanyWebsite("https://microsoft.com");
        msft.setApplicationUrl("https://careers.microsoft.com/us/en/job/182939");
        msft.setLocation("Redmond, WA");
        msft.setWorkMode(WorkMode.HYBRID);
        msft.setEmploymentType("INTERNSHIP");
        msft.setApplicationDate(today.minusDays(35));
        msft.setDeadline(today.minusDays(10));
        msft.setStipend(new BigDecimal("8800.00"));
        msft.setStatus(ApplicationStatus.OFFER_RECEIVED);
        msft.setPriority(Priority.HIGH);
        msft.setNotes("Received written offer letter! Competitive relocation stipend and return-offer pathway.");

        Interview msftR1 = new Interview();
        msftR1.setInterviewRound("Round 1: Technical Screening");
        msftR1.setInterviewDate(LocalDateTime.now().minusDays(22).withHour(11).withMinute(0));
        msftR1.setInterviewMode(InterviewMode.ONLINE);
        msftR1.setMeetingLink("https://teams.microsoft.com/l/meetup-join/msft-round1");
        msftR1.setInterviewerName("Marcus Vance (Senior Engineer)");
        msftR1.setPreparationNotes("System architecture questions and array sliding window problem.");
        msftR1.setOutcome(InterviewOutcome.PASSED);
        msft.addInterview(msftR1);

        Interview msftFinal = new Interview();
        msftFinal.setInterviewRound("Round 2: Final Loop & Hiring Manager");
        msftFinal.setInterviewDate(LocalDateTime.now().minusDays(12).withHour(14).withMinute(0));
        msftFinal.setInterviewMode(InterviewMode.ONLINE);
        msftFinal.setMeetingLink("https://teams.microsoft.com/l/meetup-join/msft-final");
        msftFinal.setInterviewerName("Karen Li (Partner Group Engineering Manager)");
        msftFinal.setPreparationNotes("Discussed distributed consensus (Raft) and cultural leadership principles.");
        msftFinal.setOutcome(InterviewOutcome.PASSED);
        msft.addInterview(msftFinal);
        applications.add(msft);

        // 3. Stripe - Interview Scheduled
        Application stripe = new Application();
        stripe.setUser(user);
        stripe.setCompanyName("Stripe");
        stripe.setJobRole("Backend Infrastructure Intern - Core Payments");
        stripe.setJobDescription("Build highly available, low-latency transaction routing infrastructure supporting global commerce.");
        stripe.setCompanyWebsite("https://stripe.com");
        stripe.setApplicationUrl("https://stripe.com/jobs/listings/swe-intern");
        stripe.setLocation("San Francisco, CA (Remote)");
        stripe.setWorkMode(WorkMode.REMOTE);
        stripe.setEmploymentType("INTERNSHIP");
        stripe.setApplicationDate(today.minusDays(18));
        stripe.setDeadline(today.plusDays(7));
        stripe.setStipend(new BigDecimal("9600.00"));
        stripe.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
        stripe.setPriority(Priority.HIGH);
        stripe.setNotes("Recruiter chat completed. 1-hour interactive pair-programming interview scheduled.");

        Interview stripePairing = new Interview();
        stripePairing.setInterviewRound("Technical Pair-Programming & API Design");
        stripePairing.setInterviewDate(LocalDateTime.now().plusDays(4).withHour(16).withMinute(0));
        stripePairing.setInterviewMode(InterviewMode.ONLINE);
        stripePairing.setMeetingLink("https://zoom.us/j/9842103851");
        stripePairing.setInterviewerName("Liam O'Connor (Staff Software Engineer)");
        stripePairing.setPreparationNotes("Review idempotency keys, HTTP status codes, and test-driven refactoring.");
        stripePairing.setOutcome(InterviewOutcome.PENDING);
        stripe.addInterview(stripePairing);
        applications.add(stripe);

        // 4. Amazon - Online Assessment
        Application amazon = new Application();
        amazon.setUser(user);
        amazon.setCompanyName("Amazon");
        amazon.setJobRole("Software Development Engineer Intern (SDE)");
        amazon.setJobDescription("Design, implement, and test core software services powering AWS or Retail customer experience.");
        amazon.setCompanyWebsite("https://amazon.jobs");
        amazon.setApplicationUrl("https://amazon.jobs/en/jobs/2418001");
        amazon.setLocation("Seattle, WA");
        amazon.setWorkMode(WorkMode.ON_SITE);
        amazon.setEmploymentType("INTERNSHIP");
        amazon.setApplicationDate(today.minusDays(8));
        amazon.setDeadline(today.plusDays(3));
        amazon.setStipend(new BigDecimal("8600.00"));
        amazon.setStatus(ApplicationStatus.ONLINE_ASSESSMENT);
        amazon.setPriority(Priority.HIGH);
        amazon.setNotes("HackerRank OA link received. 70-min coding + work simulation test due this weekend.");
        applications.add(amazon);

        // 5. Spotify - Interview Completed
        Application spotify = new Application();
        spotify.setUser(user);
        spotify.setCompanyName("Spotify");
        spotify.setJobRole("Web Platform & Player SDK Intern");
        spotify.setJobDescription("Help build the next generation of web audio streaming components and responsive desktop player UI.");
        spotify.setCompanyWebsite("https://spotify.com");
        spotify.setApplicationUrl("https://lifeatspotify.com/jobs/web-intern");
        spotify.setLocation("New York, NY");
        spotify.setWorkMode(WorkMode.HYBRID);
        spotify.setEmploymentType("INTERNSHIP");
        spotify.setApplicationDate(today.minusDays(28));
        spotify.setDeadline(today.minusDays(5));
        spotify.setStipend(new BigDecimal("8200.00"));
        spotify.setStatus(ApplicationStatus.INTERVIEW_COMPLETED);
        spotify.setPriority(Priority.MEDIUM);
        spotify.setNotes("All interview loops done. Recruiter indicated final decision by end of week.");

        Interview spotifyInterview = new Interview();
        spotifyInterview.setInterviewRound("Round 1: Architecture & Collaborative Coding");
        spotifyInterview.setInterviewDate(LocalDateTime.now().minusDays(3).withHour(13).withMinute(0));
        spotifyInterview.setInterviewMode(InterviewMode.ONLINE);
        spotifyInterview.setMeetingLink("https://meet.google.com/spo-tify-demo");
        spotifyInterview.setInterviewerName("Elena Rostova (Engineering Manager)");
        spotifyInterview.setPreparationNotes("Designed a lightweight event-emitter in JavaScript/TypeScript.");
        spotifyInterview.setOutcome(InterviewOutcome.PENDING);
        spotify.addInterview(spotifyInterview);
        applications.add(spotify);

        // 6. Datadog - Applied
        Application datadog = new Application();
        datadog.setUser(user);
        datadog.setCompanyName("Datadog");
        datadog.setJobRole("Full Stack Engineering Intern - APM Tracing");
        datadog.setJobDescription("Develop distributed tracing visualizations and agent performance collectors.");
        datadog.setCompanyWebsite("https://datadoghq.com");
        datadog.setApplicationUrl("https://careers.datadoghq.com/detail/583921");
        datadog.setLocation("Boston, MA (Remote)");
        datadog.setWorkMode(WorkMode.REMOTE);
        datadog.setEmploymentType("INTERNSHIP");
        datadog.setApplicationDate(today.minusDays(6));
        datadog.setDeadline(today.plusDays(10));
        datadog.setStipend(new BigDecimal("8400.00"));
        datadog.setStatus(ApplicationStatus.APPLIED);
        datadog.setPriority(Priority.MEDIUM);
        datadog.setNotes("Application submitted with customized cover letter highlighting APM side-project.");
        applications.add(datadog);

        // 7. Palantir - Online Assessment
        Application palantir = new Application();
        palantir.setUser(user);
        palantir.setCompanyName("Palantir Technologies");
        palantir.setJobRole("Forward Deployed Software Engineer Intern");
        palantir.setJobDescription("Deploy real-world data integration pipelines and Foundry applications for enterprise partners.");
        palantir.setCompanyWebsite("https://palantir.com");
        palantir.setApplicationUrl("https://palantir.com/careers/fdse-intern");
        palantir.setLocation("New York, NY");
        palantir.setWorkMode(WorkMode.HYBRID);
        palantir.setEmploymentType("INTERNSHIP");
        palantir.setApplicationDate(today.minusDays(5));
        palantir.setDeadline(today.plusDays(5));
        palantir.setStipend(new BigDecimal("9100.00"));
        palantir.setStatus(ApplicationStatus.ONLINE_ASSESSMENT);
        palantir.setPriority(Priority.HIGH);
        palantir.setNotes("Invitation to complete technical screening on Karat platform.");
        applications.add(palantir);

        // 8. Netflix - Applied
        Application netflix = new Application();
        netflix.setUser(user);
        netflix.setCompanyName("Netflix");
        netflix.setJobRole("Data Engineering Intern - Content & Studio Systems");
        netflix.setJobDescription("Build scalable Apache Iceberg and Kafka streams for real-time video catalog processing.");
        netflix.setCompanyWebsite("https://netflix.com");
        netflix.setApplicationUrl("https://jobs.netflix.com/jobs/data-eng-intern");
        netflix.setLocation("Los Gatos, CA (Remote)");
        netflix.setWorkMode(WorkMode.REMOTE);
        netflix.setEmploymentType("INTERNSHIP");
        netflix.setApplicationDate(today.minusDays(3));
        netflix.setDeadline(today.plusDays(18));
        netflix.setStipend(new BigDecimal("9000.00"));
        netflix.setStatus(ApplicationStatus.APPLIED);
        netflix.setPriority(Priority.MEDIUM);
        netflix.setNotes("Referred by college alumni currently working in Content Engineering.");
        applications.add(netflix);

        // 9. Bloomberg - Wishlist
        Application bloomberg = new Application();
        bloomberg.setUser(user);
        bloomberg.setCompanyName("Bloomberg LP");
        bloomberg.setJobRole("Software Engineering Intern - Real-Time Market Data");
        bloomberg.setJobDescription("Design ultra-low-latency financial quotation and tick storage engine subsystems.");
        bloomberg.setCompanyWebsite("https://bloomberg.com");
        bloomberg.setApplicationUrl("https://bloomberg.com/careers/software-engineer-intern");
        bloomberg.setLocation("New York, NY");
        bloomberg.setWorkMode(WorkMode.ON_SITE);
        bloomberg.setEmploymentType("INTERNSHIP");
        bloomberg.setApplicationDate(today.minusDays(2));
        bloomberg.setDeadline(today.plusDays(25));
        bloomberg.setStipend(new BigDecimal("8750.00"));
        bloomberg.setStatus(ApplicationStatus.WISHLIST);
        bloomberg.setPriority(Priority.MEDIUM);
        bloomberg.setNotes("Application window opens next week. Refresh C++ templates and memory model.");
        applications.add(bloomberg);

        // 10. Uber - Rejected
        Application uber = new Application();
        uber.setUser(user);
        uber.setCompanyName("Uber");
        uber.setJobRole("Frontend Engineering Intern - Rider Experience");
        uber.setJobDescription("Develop responsive web and mobile web booking flows for riders globally.");
        uber.setCompanyWebsite("https://uber.com");
        uber.setApplicationUrl("https://uber.com/careers/frontend-intern");
        uber.setLocation("San Francisco, CA");
        uber.setWorkMode(WorkMode.HYBRID);
        uber.setEmploymentType("INTERNSHIP");
        uber.setApplicationDate(today.minusDays(45));
        uber.setDeadline(today.minusDays(20));
        uber.setStipend(new BigDecimal("8200.00"));
        uber.setStatus(ApplicationStatus.REJECTED);
        uber.setPriority(Priority.LOW);
        uber.setNotes("Received automated notification that position was closed due to head-count reallocation.");
        applications.add(uber);

        // Save all applications with cascade for interviews
        applicationRepository.saveAll(applications);
        log.info("Successfully seeded {} applications with interviews for user: {}", applications.size(), user.getEmail());
    }
}

```

---

## SecurityConfig.java
**File Location**: src/main/java/com/internshiptracker/config/SecurityConfig.java

```java
package com.internshiptracker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internshiptracker.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .authorizeHttpRequests(auth -> auth
                // Public authentication endpoints
                .requestMatchers("/api/auth/**").permitAll()
                // Static resources & frontend UI pages
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/login.html",
                    "/register.html",
                    "/applications.html",
                    "/interviews.html",
                    "/css/**",
                    "/js/**",
                    "/favicon.ico"
                ).permitAll()
                // All other API endpoints must be authenticated
                .requestMatchers("/api/**").authenticated()
                // Any other static content
                .anyRequest().permitAll()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    ErrorResponse error = new ErrorResponse(
                            HttpStatus.UNAUTHORIZED.value(),
                            HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                            "Please log in to access this resource",
                            request.getRequestURI()
                    );
                    response.getWriter().write(objectMapper.writeValueAsString(error));
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    ErrorResponse error = new ErrorResponse(
                            HttpStatus.FORBIDDEN.value(),
                            HttpStatus.FORBIDDEN.getReasonPhrase(),
                            "Access denied: You do not have permission for this resource",
                            request.getRequestURI()
                    );
                    response.getWriter().write(objectMapper.writeValueAsString(error));
                })
            );

        return http.build();
    }
}

```

---

## UserPrincipal.java
**File Location**: src/main/java/com/internshiptracker/config/UserPrincipal.java

```java
package com.internshiptracker.config;

import com.internshiptracker.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String fullName;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String fullName, String email, String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserPrincipal create(User user) {
        return new UserPrincipal(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

```

---

## ApplicationController.java
**File Location**: src/main/java/com/internshiptracker/controller/ApplicationController.java

```java
package com.internshiptracker.controller;

import com.internshiptracker.dto.ApiResponse;
import com.internshiptracker.dto.ApplicationRequest;
import com.internshiptracker.dto.ApplicationResponse;
import com.internshiptracker.dto.ApplicationStatusUpdateRequest;
import com.internshiptracker.entity.ApplicationStatus;
import com.internshiptracker.entity.WorkMode;
import com.internshiptracker.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> getApplications(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) WorkMode workMode,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ApplicationResponse> applications = applicationService.getApplications(
                search, status, workMode, location, fromDate, toDate, pageable
        );

        return ResponseEntity.ok(ApiResponse.ok(applications));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getApplicationById(@PathVariable Long id) {
        ApplicationResponse response = applicationService.getApplicationById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ApplicationResponse>> createApplication(@Valid @RequestBody ApplicationRequest request) {
        ApplicationResponse response = applicationService.createApplication(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Application created successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateApplication(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationRequest request) {
        ApplicationResponse response = applicationService.updateApplication(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Application updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationStatusUpdateRequest request) {
        ApplicationResponse response = applicationService.updateStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.ok("Application status updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.ok(ApiResponse.message("Application deleted successfully"));
    }
}

```

---

## AuthController.java
**File Location**: src/main/java/com/internshiptracker/controller/AuthController.java

```java
package com.internshiptracker.controller;

import com.internshiptracker.dto.ApiResponse;
import com.internshiptracker.dto.LoginRequest;
import com.internshiptracker.dto.RegisterRequest;
import com.internshiptracker.dto.UserResponse;
import com.internshiptracker.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User registered successfully. You may now log in.", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        UserResponse response = authService.login(request, httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok(ApiResponse.message("Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse response = authService.getCurrentUserResponse();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}

```

---

## DashboardController.java
**File Location**: src/main/java/com/internshiptracker/controller/DashboardController.java

```java
package com.internshiptracker.controller;

import com.internshiptracker.dto.*;
import com.internshiptracker.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary() {
        DashboardSummaryResponse summary = dashboardService.getSummary();
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @GetMapping("/status-distribution")
    public ResponseEntity<ApiResponse<List<StatusDistributionDto>>> getStatusDistribution() {
        List<StatusDistributionDto> distribution = dashboardService.getStatusDistribution();
        return ResponseEntity.ok(ApiResponse.ok(distribution));
    }

    @GetMapping("/application-trends")
    public ResponseEntity<ApiResponse<List<ApplicationTrendDto>>> getApplicationTrends() {
        List<ApplicationTrendDto> trends = dashboardService.getApplicationTrends();
        return ResponseEntity.ok(ApiResponse.ok(trends));
    }

    @GetMapping("/upcoming-deadlines")
    public ResponseEntity<ApiResponse<List<UpcomingDeadlineDto>>> getUpcomingDeadlines() {
        List<UpcomingDeadlineDto> deadlines = dashboardService.getUpcomingDeadlines();
        return ResponseEntity.ok(ApiResponse.ok(deadlines));
    }

    @GetMapping("/upcoming-interviews")
    public ResponseEntity<ApiResponse<List<UpcomingInterviewDto>>> getUpcomingInterviews() {
        List<UpcomingInterviewDto> interviews = dashboardService.getUpcomingInterviews();
        return ResponseEntity.ok(ApiResponse.ok(interviews));
    }
}

```

---

## InterviewController.java
**File Location**: src/main/java/com/internshiptracker/controller/InterviewController.java

```java
package com.internshiptracker.controller;

import com.internshiptracker.dto.ApiResponse;
import com.internshiptracker.dto.InterviewRequest;
import com.internshiptracker.dto.InterviewResponse;
import com.internshiptracker.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @GetMapping("/applications/{applicationId}/interviews")
    public ResponseEntity<ApiResponse<List<InterviewResponse>>> getInterviewsForApplication(
            @PathVariable Long applicationId) {
        List<InterviewResponse> interviews = interviewService.getInterviewsForApplication(applicationId);
        return ResponseEntity.ok(ApiResponse.ok(interviews));
    }

    @PostMapping("/applications/{applicationId}/interviews")
    public ResponseEntity<ApiResponse<InterviewResponse>> scheduleInterview(
            @PathVariable Long applicationId,
            @Valid @RequestBody InterviewRequest request) {
        InterviewResponse response = interviewService.scheduleInterview(applicationId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Interview scheduled successfully", response));
    }

    @PutMapping("/interviews/{id}")
    public ResponseEntity<ApiResponse<InterviewResponse>> updateInterview(
            @PathVariable Long id,
            @Valid @RequestBody InterviewRequest request) {
        InterviewResponse response = interviewService.updateInterview(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Interview updated successfully", response));
    }

    @DeleteMapping("/interviews/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInterview(@PathVariable Long id) {
        interviewService.deleteInterview(id);
        return ResponseEntity.ok(ApiResponse.message("Interview deleted successfully"));
    }
}

```

---

## ApiResponse.java
**File Location**: src/main/java/com/internshiptracker/dto/ApiResponse.java

```java
package com.internshiptracker.dto;

import java.time.LocalDateTime;

public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "Operation successful", data);
    }

    public static <T> ApiResponse<T> message(String message) {
        return new ApiResponse<>(true, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

```

---

## ApplicationRequest.java
**File Location**: src/main/java/com/internshiptracker/dto/ApplicationRequest.java

```java
package com.internshiptracker.dto;

import com.internshiptracker.entity.ApplicationStatus;
import com.internshiptracker.entity.Priority;
import com.internshiptracker.entity.WorkMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ApplicationRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 150, message = "Company name cannot exceed 150 characters")
    private String companyName;

    @NotBlank(message = "Job/internship role is required")
    @Size(max = 150, message = "Job role cannot exceed 150 characters")
    private String jobRole;

    private String jobDescription;

    @Size(max = 500, message = "Application URL cannot exceed 500 characters")
    private String applicationUrl;

    @Size(max = 255, message = "Company website cannot exceed 255 characters")
    private String companyWebsite;

    @Size(max = 150, message = "Location cannot exceed 150 characters")
    private String location;

    private WorkMode workMode = WorkMode.REMOTE;

    private String employmentType = "INTERNSHIP";

    @NotNull(message = "Application date is required")
    private LocalDate applicationDate;

    private LocalDate deadline;

    @DecimalMin(value = "0.0", inclusive = true, message = "Expected stipend cannot be negative")
    private BigDecimal stipend = BigDecimal.ZERO;

    private ApplicationStatus status = ApplicationStatus.APPLIED;

    private Priority priority = Priority.MEDIUM;

    private String notes;

    public ApplicationRequest() {
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobRole() {
        return jobRole;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public void setApplicationUrl(String applicationUrl) {
        this.applicationUrl = applicationUrl;
    }

    public String getCompanyWebsite() {
        return companyWebsite;
    }

    public void setCompanyWebsite(String companyWebsite) {
        this.companyWebsite = companyWebsite;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public WorkMode getWorkMode() {
        return workMode;
    }

    public void setWorkMode(WorkMode workMode) {
        this.workMode = workMode;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public BigDecimal getStipend() {
        return stipend;
    }

    public void setStipend(BigDecimal stipend) {
        this.stipend = stipend;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

```

---

## ApplicationResponse.java
**File Location**: src/main/java/com/internshiptracker/dto/ApplicationResponse.java

```java
package com.internshiptracker.dto;

import com.internshiptracker.entity.Application;
import com.internshiptracker.entity.ApplicationStatus;
import com.internshiptracker.entity.Priority;
import com.internshiptracker.entity.WorkMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ApplicationResponse {

    private Long id;
    private String companyName;
    private String jobRole;
    private String jobDescription;
    private String applicationUrl;
    private String companyWebsite;
    private String location;
    private WorkMode workMode;
    private String employmentType;
    private LocalDate applicationDate;
    private LocalDate deadline;
    private BigDecimal stipend;
    private ApplicationStatus status;
    private Priority priority;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int interviewCount;

    public ApplicationResponse() {
    }

    public static ApplicationResponse fromEntity(Application app) {
        ApplicationResponse resp = new ApplicationResponse();
        resp.setId(app.getId());
        resp.setCompanyName(app.getCompanyName());
        resp.setJobRole(app.getJobRole());
        resp.setJobDescription(app.getJobDescription());
        resp.setApplicationUrl(app.getApplicationUrl());
        resp.setCompanyWebsite(app.getCompanyWebsite());
        resp.setLocation(app.getLocation());
        resp.setWorkMode(app.getWorkMode());
        resp.setEmploymentType(app.getEmploymentType());
        resp.setApplicationDate(app.getApplicationDate());
        resp.setDeadline(app.getDeadline());
        resp.setStipend(app.getStipend());
        resp.setStatus(app.getStatus());
        resp.setPriority(app.getPriority());
        resp.setNotes(app.getNotes());
        resp.setCreatedAt(app.getCreatedAt());
        resp.setUpdatedAt(app.getUpdatedAt());
        resp.setInterviewCount(app.getInterviews() != null ? app.getInterviews().size() : 0);
        return resp;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobRole() {
        return jobRole;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public void setApplicationUrl(String applicationUrl) {
        this.applicationUrl = applicationUrl;
    }

    public String getCompanyWebsite() {
        return companyWebsite;
    }

    public void setCompanyWebsite(String companyWebsite) {
        this.companyWebsite = companyWebsite;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public WorkMode getWorkMode() {
        return workMode;
    }

    public void setWorkMode(WorkMode workMode) {
        this.workMode = workMode;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public BigDecimal getStipend() {
        return stipend;
    }

    public void setStipend(BigDecimal stipend) {
        this.stipend = stipend;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getInterviewCount() {
        return interviewCount;
    }

    public void setInterviewCount(int interviewCount) {
        this.interviewCount = interviewCount;
    }
}

```

---

## ApplicationStatusUpdateRequest.java
**File Location**: src/main/java/com/internshiptracker/dto/ApplicationStatusUpdateRequest.java

```java
package com.internshiptracker.dto;

import com.internshiptracker.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public class ApplicationStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private ApplicationStatus status;

    public ApplicationStatusUpdateRequest() {
    }

    public ApplicationStatusUpdateRequest(ApplicationStatus status) {
        this.status = status;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
}

```

---

## ApplicationTrendDto.java
**File Location**: src/main/java/com/internshiptracker/dto/ApplicationTrendDto.java

```java
package com.internshiptracker.dto;

public class ApplicationTrendDto {

    private String date;
    private long count;

    public ApplicationTrendDto() {
    }

    public ApplicationTrendDto(String date, long count) {
        this.date = date;
        this.count = count;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}

```

---

## DashboardSummaryResponse.java
**File Location**: src/main/java/com/internshiptracker/dto/DashboardSummaryResponse.java

```java
package com.internshiptracker.dto;

public class DashboardSummaryResponse {

    private long totalApplications;
    private long applicationsSubmitted;
    private long applicationsInAssessment;
    private long interviewsScheduled;
    private long offersReceived;
    private long rejectedApplications;
    private long upcomingDeadlines;
    private long upcomingInterviews;
    private double successRate;

    public DashboardSummaryResponse() {
    }

    public long getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(long totalApplications) {
        this.totalApplications = totalApplications;
    }

    public long getApplicationsSubmitted() {
        return applicationsSubmitted;
    }

    public void setApplicationsSubmitted(long applicationsSubmitted) {
        this.applicationsSubmitted = applicationsSubmitted;
    }

    public long getApplicationsInAssessment() {
        return applicationsInAssessment;
    }

    public void setApplicationsInAssessment(long applicationsInAssessment) {
        this.applicationsInAssessment = applicationsInAssessment;
    }

    public long getInterviewsScheduled() {
        return interviewsScheduled;
    }

    public void setInterviewsScheduled(long interviewsScheduled) {
        this.interviewsScheduled = interviewsScheduled;
    }

    public long getOffersReceived() {
        return offersReceived;
    }

    public void setOffersReceived(long offersReceived) {
        this.offersReceived = offersReceived;
    }

    public long getRejectedApplications() {
        return rejectedApplications;
    }

    public void setRejectedApplications(long rejectedApplications) {
        this.rejectedApplications = rejectedApplications;
    }

    public long getUpcomingDeadlines() {
        return upcomingDeadlines;
    }

    public void setUpcomingDeadlines(long upcomingDeadlines) {
        this.upcomingDeadlines = upcomingDeadlines;
    }

    public long getUpcomingInterviews() {
        return upcomingInterviews;
    }

    public void setUpcomingInterviews(long upcomingInterviews) {
        this.upcomingInterviews = upcomingInterviews;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }
}

```

---

## ErrorResponse.java
**File Location**: src/main/java/com/internshiptracker/dto/ErrorResponse.java

```java
package com.internshiptracker.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> validationErrors;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public ErrorResponse(int status, String error, String message, String path, Map<String, String> validationErrors) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.validationErrors = validationErrors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Map<String, String> validationErrors) {
        this.validationErrors = validationErrors;
    }
}

```

---

## InterviewRequest.java
**File Location**: src/main/java/com/internshiptracker/dto/InterviewRequest.java

```java
package com.internshiptracker.dto;

import com.internshiptracker.entity.InterviewMode;
import com.internshiptracker.entity.InterviewOutcome;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class InterviewRequest {

    @NotBlank(message = "Interview round is required (e.g. Technical Round 1, HR, Screening)")
    @Size(max = 100, message = "Interview round cannot exceed 100 characters")
    private String interviewRound;

    @NotNull(message = "Interview date and time is required")
    private LocalDateTime interviewDate;

    private InterviewMode interviewMode = InterviewMode.ONLINE;

    @Size(max = 500, message = "Meeting link cannot exceed 500 characters")
    private String meetingLink;

    @Size(max = 150, message = "Interviewer name cannot exceed 150 characters")
    private String interviewerName;

    private String preparationNotes;

    private InterviewOutcome outcome = InterviewOutcome.PENDING;

    public InterviewRequest() {
    }

    public String getInterviewRound() {
        return interviewRound;
    }

    public void setInterviewRound(String interviewRound) {
        this.interviewRound = interviewRound;
    }

    public LocalDateTime getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(LocalDateTime interviewDate) {
        this.interviewDate = interviewDate;
    }

    public InterviewMode getInterviewMode() {
        return interviewMode;
    }

    public void setInterviewMode(InterviewMode interviewMode) {
        this.interviewMode = interviewMode;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }

    public String getInterviewerName() {
        return interviewerName;
    }

    public void setInterviewerName(String interviewerName) {
        this.interviewerName = interviewerName;
    }

    public String getPreparationNotes() {
        return preparationNotes;
    }

    public void setPreparationNotes(String preparationNotes) {
        this.preparationNotes = preparationNotes;
    }

    public InterviewOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(InterviewOutcome outcome) {
        this.outcome = outcome;
    }
}

```

---

## InterviewResponse.java
**File Location**: src/main/java/com/internshiptracker/dto/InterviewResponse.java

```java
package com.internshiptracker.dto;

import com.internshiptracker.entity.Interview;
import com.internshiptracker.entity.InterviewMode;
import com.internshiptracker.entity.InterviewOutcome;

import java.time.LocalDateTime;

public class InterviewResponse {

    private Long id;
    private Long applicationId;
    private String companyName;
    private String jobRole;
    private String interviewRound;
    private LocalDateTime interviewDate;
    private InterviewMode interviewMode;
    private String meetingLink;
    private String interviewerName;
    private String preparationNotes;
    private InterviewOutcome outcome;
    private LocalDateTime createdAt;

    public InterviewResponse() {
    }

    public static InterviewResponse fromEntity(Interview interview) {
        InterviewResponse resp = new InterviewResponse();
        resp.setId(interview.getId());
        if (interview.getApplication() != null) {
            resp.setApplicationId(interview.getApplication().getId());
            resp.setCompanyName(interview.getApplication().getCompanyName());
            resp.setJobRole(interview.getApplication().getJobRole());
        }
        resp.setInterviewRound(interview.getInterviewRound());
        resp.setInterviewDate(interview.getInterviewDate());
        resp.setInterviewMode(interview.getInterviewMode());
        resp.setMeetingLink(interview.getMeetingLink());
        resp.setInterviewerName(interview.getInterviewerName());
        resp.setPreparationNotes(interview.getPreparationNotes());
        resp.setOutcome(interview.getOutcome());
        resp.setCreatedAt(interview.getCreatedAt());
        return resp;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobRole() {
        return jobRole;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }

    public String getInterviewRound() {
        return interviewRound;
    }

    public void setInterviewRound(String interviewRound) {
        this.interviewRound = interviewRound;
    }

    public LocalDateTime getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(LocalDateTime interviewDate) {
        this.interviewDate = interviewDate;
    }

    public InterviewMode getInterviewMode() {
        return interviewMode;
    }

    public void setInterviewMode(InterviewMode interviewMode) {
        this.interviewMode = interviewMode;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }

    public String getInterviewerName() {
        return interviewerName;
    }

    public void setInterviewerName(String interviewerName) {
        this.interviewerName = interviewerName;
    }

    public String getPreparationNotes() {
        return preparationNotes;
    }

    public void setPreparationNotes(String preparationNotes) {
        this.preparationNotes = preparationNotes;
    }

    public InterviewOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(InterviewOutcome outcome) {
        this.outcome = outcome;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

```

---

## LoginRequest.java
**File Location**: src/main/java/com/internshiptracker/dto/LoginRequest.java

```java
package com.internshiptracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

```

---

## RegisterRequest.java
**File Location**: src/main/java/com/internshiptracker/dto/RegisterRequest.java

```java
package com.internshiptracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters long")
    private String password;

    public RegisterRequest() {
    }

    public RegisterRequest(String fullName, String email, String password) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

```

---

## StatusDistributionDto.java
**File Location**: src/main/java/com/internshiptracker/dto/StatusDistributionDto.java

```java
package com.internshiptracker.dto;

import com.internshiptracker.entity.ApplicationStatus;

public class StatusDistributionDto {

    private ApplicationStatus status;
    private String label;
    private long count;
    private String color;

    public StatusDistributionDto() {
    }

    public StatusDistributionDto(ApplicationStatus status, String label, long count, String color) {
        this.status = status;
        this.label = label;
        this.count = count;
        this.color = color;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}

```

---

## UpcomingDeadlineDto.java
**File Location**: src/main/java/com/internshiptracker/dto/UpcomingDeadlineDto.java

```java
package com.internshiptracker.dto;

import com.internshiptracker.entity.ApplicationStatus;
import com.internshiptracker.entity.Priority;

import java.time.LocalDate;

public class UpcomingDeadlineDto {

    private Long applicationId;
    private String companyName;
    private String jobRole;
    private LocalDate deadline;
    private long daysRemaining;
    private boolean overdue;
    private ApplicationStatus status;
    private Priority priority;

    public UpcomingDeadlineDto() {
    }

    public UpcomingDeadlineDto(Long applicationId, String companyName, String jobRole,
                               LocalDate deadline, long daysRemaining, boolean overdue,
                               ApplicationStatus status, Priority priority) {
        this.applicationId = applicationId;
        this.companyName = companyName;
        this.jobRole = jobRole;
        this.deadline = deadline;
        this.daysRemaining = daysRemaining;
        this.overdue = overdue;
        this.status = status;
        this.priority = priority;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobRole() {
        return jobRole;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public long getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(long daysRemaining) {
        this.daysRemaining = daysRemaining;
    }

    public boolean isOverdue() {
        return overdue;
    }

    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}

```

---

## UpcomingInterviewDto.java
**File Location**: src/main/java/com/internshiptracker/dto/UpcomingInterviewDto.java

```java
package com.internshiptracker.dto;

import com.internshiptracker.entity.InterviewMode;
import com.internshiptracker.entity.InterviewOutcome;

import java.time.LocalDateTime;

public class UpcomingInterviewDto {

    private Long interviewId;
    private Long applicationId;
    private String companyName;
    private String jobRole;
    private String interviewRound;
    private LocalDateTime interviewDate;
    private InterviewMode interviewMode;
    private String meetingLink;
    private String interviewerName;
    private InterviewOutcome outcome;

    public UpcomingInterviewDto() {
    }

    public UpcomingInterviewDto(Long interviewId, Long applicationId, String companyName,
                                String jobRole, String interviewRound, LocalDateTime interviewDate,
                                InterviewMode interviewMode, String meetingLink,
                                String interviewerName, InterviewOutcome outcome) {
        this.interviewId = interviewId;
        this.applicationId = applicationId;
        this.companyName = companyName;
        this.jobRole = jobRole;
        this.interviewRound = interviewRound;
        this.interviewDate = interviewDate;
        this.interviewMode = interviewMode;
        this.meetingLink = meetingLink;
        this.interviewerName = interviewerName;
        this.outcome = outcome;
    }

    public Long getInterviewId() {
        return interviewId;
    }

    public void setInterviewId(Long interviewId) {
        this.interviewId = interviewId;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobRole() {
        return jobRole;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }

    public String getInterviewRound() {
        return interviewRound;
    }

    public void setInterviewRound(String interviewRound) {
        this.interviewRound = interviewRound;
    }

    public LocalDateTime getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(LocalDateTime interviewDate) {
        this.interviewDate = interviewDate;
    }

    public InterviewMode getInterviewMode() {
        return interviewMode;
    }

    public void setInterviewMode(InterviewMode interviewMode) {
        this.interviewMode = interviewMode;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }

    public String getInterviewerName() {
        return interviewerName;
    }

    public void setInterviewerName(String interviewerName) {
        this.interviewerName = interviewerName;
    }

    public InterviewOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(InterviewOutcome outcome) {
        this.outcome = outcome;
    }
}

```

---

## UserResponse.java
**File Location**: src/main/java/com/internshiptracker/dto/UserResponse.java

```java
package com.internshiptracker.dto;

import java.time.LocalDateTime;

public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private LocalDateTime createdAt;

    public UserResponse() {
    }

    public UserResponse(Long id, String fullName, String email, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

```

---

## Application.java
**File Location**: src/main/java/com/internshiptracker/entity/Application.java

```java
package com.internshiptracker.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    @Column(name = "job_role", nullable = false, length = 150)
    private String jobRole;

    @Column(name = "job_description", columnDefinition = "TEXT")
    private String jobDescription;

    @Column(name = "application_url", length = 500)
    private String applicationUrl;

    @Column(name = "company_website", length = 255)
    private String companyWebsite;

    @Column(name = "location", length = 150)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode", nullable = false, length = 50)
    private WorkMode workMode = WorkMode.REMOTE;

    @Column(name = "employment_type", length = 50)
    private String employmentType = "INTERNSHIP";

    @Column(name = "application_date", nullable = false)
    private LocalDate applicationDate;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "stipend", precision = 12, scale = 2)
    private BigDecimal stipend = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 50)
    private Priority priority = Priority.MEDIUM;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Interview> interviews = new ArrayList<>();

    public Application() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.status == null) {
            this.status = ApplicationStatus.APPLIED;
        }
        if (this.workMode == null) {
            this.workMode = WorkMode.REMOTE;
        }
        if (this.priority == null) {
            this.priority = Priority.MEDIUM;
        }
        if (this.stipend == null) {
            this.stipend = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper methods for bidirectional relationships
    public void addInterview(Interview interview) {
        interviews.add(interview);
        interview.setApplication(this);
    }

    public void removeInterview(Interview interview) {
        interviews.remove(interview);
        interview.setApplication(null);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobRole() {
        return jobRole;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public void setApplicationUrl(String applicationUrl) {
        this.applicationUrl = applicationUrl;
    }

    public String getCompanyWebsite() {
        return companyWebsite;
    }

    public void setCompanyWebsite(String companyWebsite) {
        this.companyWebsite = companyWebsite;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public WorkMode getWorkMode() {
        return workMode;
    }

    public void setWorkMode(WorkMode workMode) {
        this.workMode = workMode;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public BigDecimal getStipend() {
        return stipend;
    }

    public void setStipend(BigDecimal stipend) {
        this.stipend = stipend;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Interview> getInterviews() {
        return interviews;
    }

    public void setInterviews(List<Interview> interviews) {
        this.interviews = interviews;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Application that = (Application) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

```

---

## ApplicationStatus.java
**File Location**: src/main/java/com/internshiptracker/entity/ApplicationStatus.java

```java
package com.internshiptracker.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ApplicationStatus {
    WISHLIST("Wishlist"),
    APPLIED("Applied"),
    ONLINE_ASSESSMENT("Online Assessment"),
    INTERVIEW_SCHEDULED("Interview Scheduled"),
    INTERVIEW_COMPLETED("Interview Completed"),
    OFFER_RECEIVED("Offer Received"),
    REJECTED("Rejected"),
    WITHDRAWN("Withdrawn");

    private final String displayName;

    ApplicationStatus(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static ApplicationStatus fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String normalized = text.trim();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            if (status.name().equalsIgnoreCase(normalized) ||
                status.displayName.equalsIgnoreCase(normalized) ||
                status.name().replace("_", " ").equalsIgnoreCase(normalized)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ApplicationStatus: " + text);
    }
}

```

---

## Interview.java
**File Location**: src/main/java/com/internshiptracker/entity/Interview.java

```java
package com.internshiptracker.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "interviews")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "interview_round", nullable = false, length = 100)
    private String interviewRound;

    @Column(name = "interview_date", nullable = false)
    private LocalDateTime interviewDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "interview_mode", nullable = false, length = 50)
    private InterviewMode interviewMode = InterviewMode.ONLINE;

    @Column(name = "meeting_link", length = 500)
    private String meetingLink;

    @Column(name = "interviewer_name", length = 150)
    private String interviewerName;

    @Column(name = "preparation_notes", columnDefinition = "TEXT")
    private String preparationNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 50)
    private InterviewOutcome outcome = InterviewOutcome.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Interview() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.interviewMode == null) {
            this.interviewMode = InterviewMode.ONLINE;
        }
        if (this.outcome == null) {
            this.outcome = InterviewOutcome.PENDING;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public String getInterviewRound() {
        return interviewRound;
    }

    public void setInterviewRound(String interviewRound) {
        this.interviewRound = interviewRound;
    }

    public LocalDateTime getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(LocalDateTime interviewDate) {
        this.interviewDate = interviewDate;
    }

    public InterviewMode getInterviewMode() {
        return interviewMode;
    }

    public void setInterviewMode(InterviewMode interviewMode) {
        this.interviewMode = interviewMode;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }

    public String getInterviewerName() {
        return interviewerName;
    }

    public void setInterviewerName(String interviewerName) {
        this.interviewerName = interviewerName;
    }

    public String getPreparationNotes() {
        return preparationNotes;
    }

    public void setPreparationNotes(String preparationNotes) {
        this.preparationNotes = preparationNotes;
    }

    public InterviewOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(InterviewOutcome outcome) {
        this.outcome = outcome;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Interview interview = (Interview) o;
        return Objects.equals(id, interview.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

```

---

## InterviewMode.java
**File Location**: src/main/java/com/internshiptracker/entity/InterviewMode.java

```java
package com.internshiptracker.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum InterviewMode {
    ONLINE("Online"),
    PHONE("Phone"),
    IN_PERSON("In-person");

    private final String displayName;

    InterviewMode(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static InterviewMode fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String normalized = text.trim();
        for (InterviewMode mode : InterviewMode.values()) {
            if (mode.name().equalsIgnoreCase(normalized) ||
                mode.displayName.equalsIgnoreCase(normalized) ||
                mode.name().replace("_", "-").equalsIgnoreCase(normalized) ||
                mode.name().replace("_", " ").equalsIgnoreCase(normalized)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown InterviewMode: " + text);
    }
}

```

---

## InterviewOutcome.java
**File Location**: src/main/java/com/internshiptracker/entity/InterviewOutcome.java

```java
package com.internshiptracker.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum InterviewOutcome {
    PENDING("Pending"),
    PASSED("Passed"),
    FAILED("Failed");

    private final String displayName;

    InterviewOutcome(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static InterviewOutcome fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String normalized = text.trim();
        for (InterviewOutcome outcome : InterviewOutcome.values()) {
            if (outcome.name().equalsIgnoreCase(normalized) || outcome.displayName.equalsIgnoreCase(normalized)) {
                return outcome;
            }
        }
        throw new IllegalArgumentException("Unknown InterviewOutcome: " + text);
    }
}

```

---

## Priority.java
**File Location**: src/main/java/com/internshiptracker/entity/Priority.java

```java
package com.internshiptracker.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Priority {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High");

    private final String displayName;

    Priority(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static Priority fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String normalized = text.trim();
        for (Priority p : Priority.values()) {
            if (p.name().equalsIgnoreCase(normalized) || p.displayName.equalsIgnoreCase(normalized)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Unknown Priority: " + text);
    }
}

```

---

## User.java
**File Location**: src/main/java/com/internshiptracker/entity/User.java

```java
package com.internshiptracker.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Application> applications = new ArrayList<>();

    public User() {
    }

    public User(Long id, String fullName, String email, String passwordHash) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Application> getApplications() {
        return applications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) || Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
}

```

---

## WorkMode.java
**File Location**: src/main/java/com/internshiptracker/entity/WorkMode.java

```java
package com.internshiptracker.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum WorkMode {
    REMOTE("Remote"),
    HYBRID("Hybrid"),
    ON_SITE("On-site");

    private final String displayName;

    WorkMode(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static WorkMode fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String normalized = text.trim();
        for (WorkMode mode : WorkMode.values()) {
            if (mode.name().equalsIgnoreCase(normalized) ||
                mode.displayName.equalsIgnoreCase(normalized) ||
                mode.name().replace("_", "-").equalsIgnoreCase(normalized) ||
                mode.name().replace("_", " ").equalsIgnoreCase(normalized)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown WorkMode: " + text);
    }
}

```

---

## BadRequestException.java
**File Location**: src/main/java/com/internshiptracker/exception/BadRequestException.java

```java
package com.internshiptracker.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}

```

---

## GlobalExceptionHandler.java
**File Location**: src/main/java/com/internshiptracker/exception/GlobalExceptionHandler.java

```java
package com.internshiptracker.exception;

import com.internshiptracker.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        log.warn("Bad request: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        log.warn("Unauthorized access: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Bad credentials attempt on path: {}", request.getRequestURI());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Invalid email or password",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied on path: {}", request.getRequestURI());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "You do not have permission to access this resource",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed for one or more fields",
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed JSON request: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Malformed JSON request or invalid enum value provided",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled internal server error: ", ex);
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected server error occurred. Please try again later.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

```

---

## ResourceNotFoundException.java
**File Location**: src/main/java/com/internshiptracker/exception/ResourceNotFoundException.java

```java
package com.internshiptracker.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

```

---

## UnauthorizedException.java
**File Location**: src/main/java/com/internshiptracker/exception/UnauthorizedException.java

```java
package com.internshiptracker.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

```

---

## InternshipTrackerApplication.java
**File Location**: src/main/java/com/internshiptracker/InternshipTrackerApplication.java

```java
package com.internshiptracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InternshipTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(InternshipTrackerApplication.class, args);
    }
}

```

---

## ApplicationRepository.java
**File Location**: src/main/java/com/internshiptracker/repository/ApplicationRepository.java

```java
package com.internshiptracker.repository;

import com.internshiptracker.entity.Application;
import com.internshiptracker.entity.ApplicationStatus;
import com.internshiptracker.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long>, JpaSpecificationExecutor<Application> {

    Optional<Application> findByIdAndUser(Long id, User user);

    boolean existsByIdAndUser(Long id, User user);

    List<Application> findByUserOrderByCreatedAtDesc(User user);

    long countByUser(User user);

    long countByUserAndStatus(User user, ApplicationStatus status);

    @Query("SELECT a.status, COUNT(a) FROM Application a WHERE a.user = :user GROUP BY a.status")
    List<Object[]> countByStatusForUser(@Param("user") User user);

    @Query("SELECT a.applicationDate, COUNT(a) FROM Application a WHERE a.user = :user AND a.applicationDate >= :sinceDate GROUP BY a.applicationDate ORDER BY a.applicationDate ASC")
    List<Object[]> countByApplicationDateSince(@Param("user") User user, @Param("sinceDate") LocalDate sinceDate);

    @Query("SELECT a FROM Application a WHERE a.user = :user AND a.deadline IS NOT NULL AND a.deadline BETWEEN :startDate AND :endDate AND a.status NOT IN :excludedStatuses ORDER BY a.deadline ASC")
    List<Application> findUpcomingDeadlines(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludedStatuses") List<ApplicationStatus> excludedStatuses);

    @Query("SELECT a FROM Application a WHERE a.user = :user AND a.deadline IS NOT NULL AND a.deadline < :currentDate AND a.status NOT IN :excludedStatuses ORDER BY a.deadline ASC")
    List<Application> findOverdueDeadlines(
            @Param("user") User user,
            @Param("currentDate") LocalDate currentDate,
            @Param("excludedStatuses") List<ApplicationStatus> excludedStatuses);

    List<Application> findTop5ByUserOrderByCreatedAtDesc(User user);
}

```

---

## ApplicationSpecification.java
**File Location**: src/main/java/com/internshiptracker/repository/ApplicationSpecification.java

```java
package com.internshiptracker.repository;

import com.internshiptracker.entity.Application;
import com.internshiptracker.entity.ApplicationStatus;
import com.internshiptracker.entity.User;
import com.internshiptracker.entity.WorkMode;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ApplicationSpecification {

    public static Specification<Application> filter(
            User user,
            String search,
            ApplicationStatus status,
            WorkMode workMode,
            String location,
            LocalDate fromDate,
            LocalDate toDate) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Strictly filter by current authenticated user
            predicates.add(cb.equal(root.get("user"), user));

            // Search by company name OR job role
            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate companyMatch = cb.like(cb.lower(root.get("companyName")), pattern);
                Predicate roleMatch = cb.like(cb.lower(root.get("jobRole")), pattern);
                predicates.add(cb.or(companyMatch, roleMatch));
            }

            // Filter by status
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // Filter by work mode
            if (workMode != null) {
                predicates.add(cb.equal(root.get("workMode"), workMode));
            }

            // Filter by location
            if (location != null && !location.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.trim().toLowerCase() + "%"));
            }

            // Filter by application date range
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("applicationDate"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("applicationDate"), toDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

```

---

## InterviewRepository.java
**File Location**: src/main/java/com/internshiptracker/repository/InterviewRepository.java

```java
package com.internshiptracker.repository;

import com.internshiptracker.entity.Interview;
import com.internshiptracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByApplicationIdAndApplicationUserOrderByInterviewDateAsc(Long applicationId, User user);

    Optional<Interview> findByIdAndApplicationUser(Long id, User user);

    @Query("SELECT i FROM Interview i WHERE i.application.user = :user AND i.interviewDate >= :fromDate ORDER BY i.interviewDate ASC")
    List<Interview> findUpcomingInterviewsForUser(@Param("user") User user, @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT COUNT(i) FROM Interview i WHERE i.application.user = :user AND i.interviewDate >= :fromDate")
    long countUpcomingInterviewsForUser(@Param("user") User user, @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT i FROM Interview i WHERE i.application.user = :user ORDER BY i.interviewDate DESC")
    List<Interview> findAllInterviewsForUser(@Param("user") User user);
}

```

---

## UserRepository.java
**File Location**: src/main/java/com/internshiptracker/repository/UserRepository.java

```java
package com.internshiptracker.repository;

import com.internshiptracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}

```

---

## ApplicationService.java
**File Location**: src/main/java/com/internshiptracker/service/ApplicationService.java

```java
package com.internshiptracker.service;

import com.internshiptracker.dto.ApplicationRequest;
import com.internshiptracker.dto.ApplicationResponse;
import com.internshiptracker.entity.Application;
import com.internshiptracker.entity.ApplicationStatus;
import com.internshiptracker.entity.Priority;
import com.internshiptracker.entity.User;
import com.internshiptracker.entity.WorkMode;
import com.internshiptracker.exception.BadRequestException;
import com.internshiptracker.exception.ResourceNotFoundException;
import com.internshiptracker.repository.ApplicationRepository;
import com.internshiptracker.repository.ApplicationSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final AuthService authService;

    public ApplicationService(ApplicationRepository applicationRepository, AuthService authService) {
        this.applicationRepository = applicationRepository;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getApplications(
            String search,
            ApplicationStatus status,
            WorkMode workMode,
            String location,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable) {

        User currentUser = authService.getCurrentUser();
        Specification<Application> spec = ApplicationSpecification.filter(
                currentUser, search, status, workMode, location, fromDate, toDate
        );

        return applicationRepository.findAll(spec, pageable)
                .map(ApplicationResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long id) {
        Application application = getApplicationEntity(id);
        return ApplicationResponse.fromEntity(application);
    }

    @Transactional(readOnly = true)
    public Application getApplicationEntity(Long id) {
        User currentUser = authService.getCurrentUser();
        return applicationRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
    }

    @Transactional
    public ApplicationResponse createApplication(ApplicationRequest request) {
        User currentUser = authService.getCurrentUser();
        validateApplicationData(request.getApplicationDate(), request.getDeadline(), request.getStipend());

        Application application = new Application();
        application.setUser(currentUser);
        mapRequestToEntity(request, application);

        Application saved = applicationRepository.save(application);
        return ApplicationResponse.fromEntity(saved);
    }

    @Transactional
    public ApplicationResponse updateApplication(Long id, ApplicationRequest request) {
        Application application = getApplicationEntity(id);
        validateApplicationData(request.getApplicationDate(), request.getDeadline(), request.getStipend());

        mapRequestToEntity(request, application);
        Application updated = applicationRepository.save(application);
        return ApplicationResponse.fromEntity(updated);
    }

    @Transactional
    public ApplicationResponse updateStatus(Long id, ApplicationStatus newStatus) {
        if (newStatus == null) {
            throw new BadRequestException("Status cannot be null");
        }
        Application application = getApplicationEntity(id);
        application.setStatus(newStatus);
        Application updated = applicationRepository.save(application);
        return ApplicationResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteApplication(Long id) {
        Application application = getApplicationEntity(id);
        applicationRepository.delete(application);
    }

    private void mapRequestToEntity(ApplicationRequest request, Application application) {
        application.setCompanyName(request.getCompanyName().trim());
        application.setJobRole(request.getJobRole().trim());
        application.setJobDescription(request.getJobDescription());
        application.setApplicationUrl(request.getApplicationUrl());
        application.setCompanyWebsite(request.getCompanyWebsite());
        application.setLocation(request.getLocation());
        application.setWorkMode(request.getWorkMode() != null ? request.getWorkMode() : WorkMode.REMOTE);
        application.setEmploymentType(request.getEmploymentType() != null ? request.getEmploymentType() : "INTERNSHIP");
        application.setApplicationDate(request.getApplicationDate());
        application.setDeadline(request.getDeadline());
        application.setStipend(request.getStipend() != null ? request.getStipend() : BigDecimal.ZERO);
        application.setStatus(request.getStatus() != null ? request.getStatus() : ApplicationStatus.APPLIED);
        application.setPriority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM);
        application.setNotes(request.getNotes());
    }

    private void validateApplicationData(LocalDate applicationDate, LocalDate deadline, BigDecimal stipend) {
        if (applicationDate != null && deadline != null && deadline.isBefore(applicationDate)) {
            throw new BadRequestException("Application deadline (" + deadline + ") cannot precede the application date (" + applicationDate + ")");
        }
        if (stipend != null && stipend.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Stipend cannot be negative");
        }
    }
}

```

---

## AuthService.java
**File Location**: src/main/java/com/internshiptracker/service/AuthService.java

```java
package com.internshiptracker.service;

import com.internshiptracker.config.UserPrincipal;
import com.internshiptracker.dto.LoginRequest;
import com.internshiptracker.dto.RegisterRequest;
import com.internshiptracker.dto.UserResponse;
import com.internshiptracker.entity.User;
import com.internshiptracker.exception.BadRequestException;
import com.internshiptracker.exception.UnauthorizedException;
import com.internshiptracker.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       SecurityContextRepository securityContextRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("An account with this email address already exists");
        }

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getCreatedAt()
        );
    }

    public UserResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("User is not authenticated");
        }

        String email;
        if (authentication.getPrincipal() instanceof UserPrincipal principal) {
            email = principal.getEmail();
        } else {
            email = authentication.getName();
        }

        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found in database"));
    }

    public UserResponse getCurrentUserResponse() {
        User user = getCurrentUser();
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}

```

---

## DashboardService.java
**File Location**: src/main/java/com/internshiptracker/service/DashboardService.java

```java
package com.internshiptracker.service;

import com.internshiptracker.dto.*;
import com.internshiptracker.entity.Application;
import com.internshiptracker.entity.ApplicationStatus;
import com.internshiptracker.entity.User;
import com.internshiptracker.repository.ApplicationRepository;
import com.internshiptracker.repository.InterviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final AuthService authService;

    // Terminal statuses excluded from active deadline reminders
    private static final List<ApplicationStatus> TERMINAL_STATUSES = List.of(
            ApplicationStatus.OFFER_RECEIVED,
            ApplicationStatus.REJECTED,
            ApplicationStatus.WITHDRAWN
    );

    public DashboardService(ApplicationRepository applicationRepository,
                            InterviewRepository interviewRepository,
                            AuthService authService) {
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        User currentUser = authService.getCurrentUser();
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysLater = today.plusDays(7);
        LocalDateTime now = LocalDateTime.now();

        long total = applicationRepository.countByUser(currentUser);
        long applied = applicationRepository.countByUserAndStatus(currentUser, ApplicationStatus.APPLIED);
        long inAssessment = applicationRepository.countByUserAndStatus(currentUser, ApplicationStatus.ONLINE_ASSESSMENT);
        long interviewScheduled = applicationRepository.countByUserAndStatus(currentUser, ApplicationStatus.INTERVIEW_SCHEDULED)
                + applicationRepository.countByUserAndStatus(currentUser, ApplicationStatus.INTERVIEW_COMPLETED);
        long offers = applicationRepository.countByUserAndStatus(currentUser, ApplicationStatus.OFFER_RECEIVED);
        long rejected = applicationRepository.countByUserAndStatus(currentUser, ApplicationStatus.REJECTED);

        List<Application> deadlines = applicationRepository.findUpcomingDeadlines(currentUser, today, sevenDaysLater, TERMINAL_STATUSES);
        long upcomingDeadlinesCount = deadlines.size();

        long upcomingInterviewsCount = interviewRepository.countUpcomingInterviewsForUser(currentUser, now);

        double successRate = 0.0;
        long resolved = offers + rejected;
        if (resolved > 0) {
            successRate = BigDecimal.valueOf((double) offers / resolved * 100.0)
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        } else if (total > 0) {
            successRate = BigDecimal.valueOf((double) offers / total * 100.0)
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        DashboardSummaryResponse response = new DashboardSummaryResponse();
        response.setTotalApplications(total);
        response.setApplicationsSubmitted(applied);
        response.setApplicationsInAssessment(inAssessment);
        response.setInterviewsScheduled(interviewScheduled);
        response.setOffersReceived(offers);
        response.setRejectedApplications(rejected);
        response.setUpcomingDeadlines(upcomingDeadlinesCount);
        response.setUpcomingInterviews(upcomingInterviewsCount);
        response.setSuccessRate(successRate);

        return response;
    }

    @Transactional(readOnly = true)
    public List<StatusDistributionDto> getStatusDistribution() {
        User currentUser = authService.getCurrentUser();
        List<Object[]> results = applicationRepository.countByStatusForUser(currentUser);

        Map<ApplicationStatus, Long> countsByStatus = new EnumMap<>(ApplicationStatus.class);
        for (Object[] row : results) {
            ApplicationStatus status = (ApplicationStatus) row[0];
            Long count = ((Number) row[1]).longValue();
            countsByStatus.put(status, count);
        }

        Map<ApplicationStatus, String> colorMap = Map.of(
                ApplicationStatus.WISHLIST, "#6c757d",
                ApplicationStatus.APPLIED, "#0d6efd",
                ApplicationStatus.ONLINE_ASSESSMENT, "#0dcaf0",
                ApplicationStatus.INTERVIEW_SCHEDULED, "#ffc107",
                ApplicationStatus.INTERVIEW_COMPLETED, "#fd7e14",
                ApplicationStatus.OFFER_RECEIVED, "#198754",
                ApplicationStatus.REJECTED, "#dc3545",
                ApplicationStatus.WITHDRAWN, "#212529"
        );

        List<StatusDistributionDto> distribution = new ArrayList<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            long count = countsByStatus.getOrDefault(status, 0L);
            distribution.add(new StatusDistributionDto(
                    status,
                    status.getDisplayName(),
                    count,
                    colorMap.getOrDefault(status, "#6c757d")
            ));
        }

        return distribution;
    }

    @Transactional(readOnly = true)
    public List<ApplicationTrendDto> getApplicationTrends() {
        User currentUser = authService.getCurrentUser();
        LocalDate sinceDate = LocalDate.now().minusDays(30);

        List<Object[]> results = applicationRepository.countByApplicationDateSince(currentUser, sinceDate);
        Map<LocalDate, Long> dateCountMap = new HashMap<>();

        for (Object[] row : results) {
            LocalDate date = (LocalDate) row[0];
            Long count = ((Number) row[1]).longValue();
            dateCountMap.put(date, count);
        }

        // Fill past 30 days continuous timeline so chart is smooth and readable
        List<ApplicationTrendDto> trends = new ArrayList<>();
        for (int i = 30; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            trends.add(new ApplicationTrendDto(d.toString(), dateCountMap.getOrDefault(d, 0L)));
        }

        return trends;
    }

    @Transactional(readOnly = true)
    public List<UpcomingDeadlineDto> getUpcomingDeadlines() {
        User currentUser = authService.getCurrentUser();
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysLater = today.plusDays(7);

        List<Application> upcoming = applicationRepository.findUpcomingDeadlines(currentUser, today, sevenDaysLater, TERMINAL_STATUSES);
        List<Application> overdue = applicationRepository.findOverdueDeadlines(currentUser, today, TERMINAL_STATUSES);

        List<UpcomingDeadlineDto> result = new ArrayList<>();

        // Add overdue deadlines first with visual distinction
        for (Application app : overdue) {
            long daysPassed = ChronoUnit.DAYS.between(app.getDeadline(), today);
            result.add(new UpcomingDeadlineDto(
                    app.getId(),
                    app.getCompanyName(),
                    app.getJobRole(),
                    app.getDeadline(),
                    -daysPassed,
                    true,
                    app.getStatus(),
                    app.getPriority()
            ));
        }

        // Add upcoming within 7 days
        for (Application app : upcoming) {
            long daysRemaining = ChronoUnit.DAYS.between(today, app.getDeadline());
            result.add(new UpcomingDeadlineDto(
                    app.getId(),
                    app.getCompanyName(),
                    app.getJobRole(),
                    app.getDeadline(),
                    daysRemaining,
                    false,
                    app.getStatus(),
                    app.getPriority()
            ));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<UpcomingInterviewDto> getUpcomingInterviews() {
        User currentUser = authService.getCurrentUser();
        LocalDateTime now = LocalDateTime.now();

        return interviewRepository.findUpcomingInterviewsForUser(currentUser, now)
                .stream()
                .map(i -> new UpcomingInterviewDto(
                        i.getId(),
                        i.getApplication().getId(),
                        i.getApplication().getCompanyName(),
                        i.getApplication().getJobRole(),
                        i.getInterviewRound(),
                        i.getInterviewDate(),
                        i.getInterviewMode(),
                        i.getMeetingLink(),
                        i.getInterviewerName(),
                        i.getOutcome()
                ))
                .collect(Collectors.toList());
    }
}

```

---

## InterviewService.java
**File Location**: src/main/java/com/internshiptracker/service/InterviewService.java

```java
package com.internshiptracker.service;

import com.internshiptracker.dto.InterviewRequest;
import com.internshiptracker.dto.InterviewResponse;
import com.internshiptracker.dto.UpcomingInterviewDto;
import com.internshiptracker.entity.*;
import com.internshiptracker.exception.BadRequestException;
import com.internshiptracker.exception.ResourceNotFoundException;
import com.internshiptracker.repository.ApplicationRepository;
import com.internshiptracker.repository.InterviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final AuthService authService;

    public InterviewService(InterviewRepository interviewRepository,
                            ApplicationRepository applicationRepository,
                            AuthService authService) {
        this.interviewRepository = interviewRepository;
        this.applicationRepository = applicationRepository;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getInterviewsForApplication(Long applicationId) {
        User currentUser = authService.getCurrentUser();
        // Verify application belongs to current user
        applicationRepository.findByIdAndUser(applicationId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        return interviewRepository.findByApplicationIdAndApplicationUserOrderByInterviewDateAsc(applicationId, currentUser)
                .stream()
                .map(InterviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public InterviewResponse scheduleInterview(Long applicationId, InterviewRequest request) {
        User currentUser = authService.getCurrentUser();
        Application application = applicationRepository.findByIdAndUser(applicationId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        if (request.getInterviewDate() == null) {
            throw new BadRequestException("Interview date and time cannot be null");
        }

        Interview interview = new Interview();
        interview.setApplication(application);
        interview.setInterviewRound(request.getInterviewRound().trim());
        interview.setInterviewDate(request.getInterviewDate());
        interview.setInterviewMode(request.getInterviewMode() != null ? request.getInterviewMode() : InterviewMode.ONLINE);
        interview.setMeetingLink(request.getMeetingLink());
        interview.setInterviewerName(request.getInterviewerName());
        interview.setPreparationNotes(request.getPreparationNotes());
        interview.setOutcome(request.getOutcome() != null ? request.getOutcome() : InterviewOutcome.PENDING);

        // If application is in prior stage, automatically move to INTERVIEW_SCHEDULED
        if (application.getStatus() == ApplicationStatus.WISHLIST ||
            application.getStatus() == ApplicationStatus.APPLIED ||
            application.getStatus() == ApplicationStatus.ONLINE_ASSESSMENT) {
            application.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
            applicationRepository.save(application);
        }

        Interview saved = interviewRepository.save(interview);
        return InterviewResponse.fromEntity(saved);
    }

    @Transactional
    public InterviewResponse updateInterview(Long id, InterviewRequest request) {
        User currentUser = authService.getCurrentUser();
        Interview interview = interviewRepository.findByIdAndApplicationUser(id, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found with id: " + id));

        if (request.getInterviewDate() == null) {
            throw new BadRequestException("Interview date and time cannot be null");
        }

        interview.setInterviewRound(request.getInterviewRound().trim());
        interview.setInterviewDate(request.getInterviewDate());
        interview.setInterviewMode(request.getInterviewMode() != null ? request.getInterviewMode() : InterviewMode.ONLINE);
        interview.setMeetingLink(request.getMeetingLink());
        interview.setInterviewerName(request.getInterviewerName());
        interview.setPreparationNotes(request.getPreparationNotes());
        interview.setOutcome(request.getOutcome() != null ? request.getOutcome() : InterviewOutcome.PENDING);

        Interview updated = interviewRepository.save(interview);
        return InterviewResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteInterview(Long id) {
        User currentUser = authService.getCurrentUser();
        Interview interview = interviewRepository.findByIdAndApplicationUser(id, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found with id: " + id));

        interviewRepository.delete(interview);
    }

    @Transactional(readOnly = true)
    public List<UpcomingInterviewDto> getUpcomingInterviews() {
        User currentUser = authService.getCurrentUser();
        LocalDateTime now = LocalDateTime.now();

        return interviewRepository.findUpcomingInterviewsForUser(currentUser, now)
                .stream()
                .map(i -> new UpcomingInterviewDto(
                        i.getId(),
                        i.getApplication().getId(),
                        i.getApplication().getCompanyName(),
                        i.getApplication().getJobRole(),
                        i.getInterviewRound(),
                        i.getInterviewDate(),
                        i.getInterviewMode(),
                        i.getMeetingLink(),
                        i.getInterviewerName(),
                        i.getOutcome()
                ))
                .collect(Collectors.toList());
    }
}

```

---

