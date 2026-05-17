package com.bytecodetracker.dto;

import com.bytecodetracker.model.RiskLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MethodAnalysisDTO {
    private String methodName;
    private String status;
    private RiskLevel riskLevel;
    private String riskReason;
}
