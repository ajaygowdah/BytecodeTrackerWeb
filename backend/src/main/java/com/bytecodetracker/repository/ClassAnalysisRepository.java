package com.bytecodetracker.repository;

import com.bytecodetracker.model.ClassAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassAnalysisRepository extends JpaRepository<ClassAnalysis, Long> {
}
