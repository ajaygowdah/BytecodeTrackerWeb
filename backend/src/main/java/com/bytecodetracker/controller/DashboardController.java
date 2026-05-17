package com.bytecodetracker.controller;

import com.bytecodetracker.dto.DashboardStatsDTO;
import com.bytecodetracker.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> stats(org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(dashboardService.getStats(authentication));
    }
}
