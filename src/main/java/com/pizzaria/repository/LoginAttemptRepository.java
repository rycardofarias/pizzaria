package com.pizzaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pizzaria.entity.LoginAttempt;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    List<LoginAttempt> findByEmailAndAttemptTimeAfter(String email, LocalDateTime after);

    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.email = :email AND la.success = false AND la.attemptTime > :after")
    int countFailedAttempts(@Param("email") String email, @Param("after") LocalDateTime after);

    @Query("SELECT la FROM LoginAttempt la WHERE la.email = :email AND la.success = false AND la.attemptTime > :after ORDER BY la.attemptTime DESC")
    List<LoginAttempt> findRecentFailedAttempts(@Param("email") String email, @Param("after") LocalDateTime after);
}
