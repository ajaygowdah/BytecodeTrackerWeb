package com.bytecodetracker.repository;

import com.bytecodetracker.model.RiskLevel;
import com.bytecodetracker.model.Scan;
import com.bytecodetracker.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ScanRepository extends JpaRepository<Scan, Long> {

    Page<Scan> findByUser(User user, Pageable pageable);

    Optional<Scan> findByIdAndUser(Long id, User user);

    long countByRiskLevel(RiskLevel riskLevel);

    long countByUser(User user);

    long countByUserAndRiskLevel(User user, RiskLevel riskLevel);

    java.util.List<Scan> findAllByUser(User user);

    @Query("""
        SELECT s FROM Scan s
        WHERE (:username IS NULL OR LOWER(s.user.username) LIKE LOWER(CONCAT('%', :username, '%')))
          AND (:riskLevel IS NULL OR s.riskLevel = :riskLevel)
          AND (:startDate IS NULL OR s.createdAt >= :startDate)
          AND (:endDate IS NULL OR s.createdAt <= :endDate)
        """)
    Page<Scan> searchForAdmin(
            @Param("username") String username,
            @Param("riskLevel") RiskLevel riskLevel,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
