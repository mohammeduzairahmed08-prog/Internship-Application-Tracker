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
