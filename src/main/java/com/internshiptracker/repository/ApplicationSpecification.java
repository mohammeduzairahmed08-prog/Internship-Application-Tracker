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
