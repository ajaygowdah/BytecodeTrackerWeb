package com.bytecodetracker.controller;

import com.bytecodetracker.dto.*;
import com.bytecodetracker.service.AdminService;
import com.bytecodetracker.service.ScanService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final ScanService scanService;

    public AdminController(AdminService adminService, ScanService scanService) {
        this.adminService = adminService;
        this.scanService = scanService;
    }

    @GetMapping("/users")
    public ResponseEntity<PagedResponse<AdminUserDTO>> users(@RequestParam(required = false) String search,
                                                             @RequestParam(required = false) String role,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.getUsers(search, role, page, size));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(new MessageResponse("User deleted"));
    }

    @GetMapping("/scans")
    public ResponseEntity<PagedResponse<AdminScanDTO>> scans(@RequestParam(required = false) String username,
                                                             @RequestParam(required = false) String riskLevel,
                                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.getScans(username, riskLevel, startDate, endDate, page, size));
    }

    @DeleteMapping("/scans/{id}")
    public ResponseEntity<MessageResponse> deleteScan(@PathVariable Long id) {
        adminService.deleteScan(id);
        return ResponseEntity.ok(new MessageResponse("Scan deleted"));
    }

    @DeleteMapping("/scans")
    public ResponseEntity<MessageResponse> deleteBulkScans(@Valid @RequestBody DeleteBulkRequest request) {
        scanService.deleteByIds(request.getIds());
        return ResponseEntity.ok(new MessageResponse("Bulk delete completed"));
    }

    @GetMapping("/reports/csv")
    public ResponseEntity<String> reportCsv() {
        String csv = adminService.exportCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=scans-report.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv);
    }
}
