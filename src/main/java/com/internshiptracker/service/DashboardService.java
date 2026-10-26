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
