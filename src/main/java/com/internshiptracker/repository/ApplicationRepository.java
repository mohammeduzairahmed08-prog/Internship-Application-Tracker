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
