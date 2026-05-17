package com.bytecodetracker.dto;

import com.bytecodetracker.model.RiskLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ViolationDTO {
    private String methodName;
    private RiskLevel riskLevel;
    private String reason;
}
