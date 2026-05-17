package com.bytecodetracker.dto;

import com.bytecodetracker.model.RiskLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ScanResultDTO {
    private Long scanId;
    private String username;
    private String fileName;
    private Integer fileSize;
    private Integer totalMethods;
    private Integer dangerousCount;
    private Integer safeCount;
    private RiskLevel riskLevel;
    private LocalDateTime createdAt;
    private List<ClassAnalysisDTO> classes;
    private List<ViolationDTO> violations;
}
