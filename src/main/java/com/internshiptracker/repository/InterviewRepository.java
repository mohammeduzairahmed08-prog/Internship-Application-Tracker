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
