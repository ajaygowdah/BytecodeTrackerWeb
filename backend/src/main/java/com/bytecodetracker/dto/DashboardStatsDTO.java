package com.bytecodetracker.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class DashboardStatsDTO {
    private long totalUsers;
    private long totalScans;
    private long highRiskScans;
    private LocalDateTime mostRecentScan;
    private List<RiskSliceDTO> riskBreakdown;
    private List<TimelinePointDTO> timeline;
}
