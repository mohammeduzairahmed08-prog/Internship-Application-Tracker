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
