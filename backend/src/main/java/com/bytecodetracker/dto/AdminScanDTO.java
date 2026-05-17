package com.bytecodetracker.dto;

import com.bytecodetracker.model.RiskLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminScanDTO {
    private Long scanId;
    private String username;
    private String filename;
    private RiskLevel riskLevel;
    private Integer totalMethods;
    private Integer dangerousCount;
    private Integer safeCount;
    private LocalDateTime createdAt;
}
