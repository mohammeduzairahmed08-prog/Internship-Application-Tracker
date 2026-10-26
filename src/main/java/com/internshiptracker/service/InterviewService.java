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
