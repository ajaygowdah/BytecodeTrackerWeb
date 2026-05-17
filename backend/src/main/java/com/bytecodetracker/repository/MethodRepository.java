package com.bytecodetracker.repository;

import com.bytecodetracker.model.MethodAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MethodRepository extends JpaRepository<MethodAnalysis, Long> {

    @Query("SELECT COUNT(m) FROM MethodAnalysis m WHERE m.riskLevel = com.bytecodetracker.model.RiskLevel.HIGH")
    long countHighRiskMethods();
}
