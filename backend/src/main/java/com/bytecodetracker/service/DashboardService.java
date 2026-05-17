package com.bytecodetracker.service;

import com.bytecodetracker.dto.DashboardStatsDTO;
import com.bytecodetracker.dto.RiskSliceDTO;
import com.bytecodetracker.dto.TimelinePointDTO;
import com.bytecodetracker.model.RiskLevel;
import com.bytecodetracker.repository.MethodRepository;
import com.bytecodetracker.repository.ScanRepository;
import com.bytecodetracker.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final ScanRepository scanRepository;
    private final MethodRepository methodRepository;

    public DashboardService(UserRepository userRepository, ScanRepository scanRepository, MethodRepository methodRepository) {
        this.userRepository = userRepository;
        this.scanRepository = scanRepository;
        this.methodRepository = methodRepository;
    }

        public DashboardStatsDTO getStats(org.springframework.security.core.Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
            .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (isAdmin) {
            long totalUsers = userRepository.count();
            long totalScans = scanRepository.count();
            long highRiskScans = scanRepository.countByRiskLevel(RiskLevel.HIGH);

            java.time.LocalDateTime mostRecent = scanRepository.findAll().stream()
                .map(scan -> scan.getCreatedAt())
                .max(Comparator.naturalOrder())
                .orElse(null);

            List<RiskSliceDTO> riskBreakdown = List.of(
                new RiskSliceDTO("HIGH", highRiskScans),
                new RiskSliceDTO("MEDIUM", scanRepository.countByRiskLevel(RiskLevel.MEDIUM)),
                new RiskSliceDTO("LOW", scanRepository.countByRiskLevel(RiskLevel.LOW))
            );

            List<TimelinePointDTO> timeline = buildTimeline(scanRepository.findAll().stream()
                .map(scan -> scan.getCreatedAt())
                .toList());

            return DashboardStatsDTO.builder()
                .totalUsers(totalUsers)
                .totalScans(totalScans)
                .highRiskScans(highRiskScans)
                .mostRecentScan(mostRecent)
                .riskBreakdown(riskBreakdown)
                .timeline(timeline)
                .build();
        } else {
            String username = authentication == null ? null : authentication.getName();
            var userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
            return DashboardStatsDTO.builder()
                .totalUsers(0)
                .totalScans(0)
                .highRiskScans(0)
                .mostRecentScan(null)
                .riskBreakdown(List.of(new RiskSliceDTO("HIGH", 0), new RiskSliceDTO("MEDIUM", 0), new RiskSliceDTO("LOW", 0)))
                .timeline(buildTimeline(List.of()))
                .build();
            }

            var user = userOpt.get();
            long totalScans = scanRepository.countByUser(user);
            long highRiskScans = scanRepository.countByUserAndRiskLevel(user, RiskLevel.HIGH);

            java.time.LocalDateTime mostRecent = scanRepository.findAllByUser(user).stream()
                .map(scan -> scan.getCreatedAt())
                .max(Comparator.naturalOrder())
                .orElse(null);

            List<RiskSliceDTO> riskBreakdown = List.of(
                new RiskSliceDTO("HIGH", highRiskScans),
                new RiskSliceDTO("MEDIUM", scanRepository.countByUserAndRiskLevel(user, RiskLevel.MEDIUM)),
                new RiskSliceDTO("LOW", scanRepository.countByUserAndRiskLevel(user, RiskLevel.LOW))
            );

            List<TimelinePointDTO> timeline = buildTimeline(scanRepository.findAllByUser(user).stream()
                .map(scan -> scan.getCreatedAt())
                .toList());

            return DashboardStatsDTO.builder()
                .totalUsers(1)
                .totalScans(totalScans)
                .highRiskScans(highRiskScans)
                .mostRecentScan(mostRecent)
                .riskBreakdown(riskBreakdown)
                .timeline(timeline)
                .build();
        }
        }

    public long totalDangerousMethods() {
        return methodRepository.countHighRiskMethods();
    }

    private List<TimelinePointDTO> buildTimeline(List<LocalDateTime> createdAtList) {
        List<TimelinePointDTO> points = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long count = createdAtList.stream().filter(dateTime -> dateTime.toLocalDate().isEqual(date)).count();
            points.add(new TimelinePointDTO(formatter.format(date), count));
        }

        return points;
    }
}
