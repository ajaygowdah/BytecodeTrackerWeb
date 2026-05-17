package com.bytecodetracker.service;

import com.bytecodetracker.dto.AdminScanDTO;
import com.bytecodetracker.dto.AdminUserDTO;
import com.bytecodetracker.dto.PagedResponse;
import com.bytecodetracker.model.RiskLevel;
import com.bytecodetracker.model.Scan;
import com.bytecodetracker.model.User;
import com.bytecodetracker.repository.ScanRepository;
import com.bytecodetracker.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ScanRepository scanRepository;

    public AdminService(UserRepository userRepository, ScanRepository scanRepository) {
        this.userRepository = userRepository;
        this.scanRepository = scanRepository;
    }

    @Transactional
    public PagedResponse<AdminUserDTO> getUsers(String search, String role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> usersPage = userRepository.findAll(pageable);
        List<AdminUserDTO> filtered = usersPage.getContent().stream()
            .map(this::toUserDto)
            .filter(dto -> matchesUserFilters(dto, search, role))
            .toList();
        Page<AdminUserDTO> mapped = new PageImpl<>(filtered, pageable, usersPage.getTotalElements());
        return PagedResponse.from(mapped);
    }

    @Transactional
    public PagedResponse<AdminScanDTO> getScans(String username, String risk, LocalDate startDate, LocalDate endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        RiskLevel riskLevel = risk == null || risk.isBlank() ? null : RiskLevel.valueOf(risk.toUpperCase());
        LocalDateTime start = startDate == null ? null : startDate.atStartOfDay();
        LocalDateTime end = endDate == null ? null : endDate.atTime(LocalTime.MAX);

        Page<AdminScanDTO> mapped = scanRepository.searchForAdmin(username, riskLevel, start, end, pageable)
                .map(this::toScanDto);
        return PagedResponse.from(mapped);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public void deleteScan(Long id) {
        if (!scanRepository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "Scan not found");
        }
        scanRepository.deleteById(id);
    }

    @Transactional
    public String exportCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("scan_id,username,filename,risk_level,total_methods,dangerous_count,safe_count,created_at\n");
        for (Scan scan : scanRepository.findAll()) {
            csv.append(scan.getId()).append(',')
                    .append(scan.getUser().getUsername()).append(',')
                    .append(sanitize(scan.getFileName())).append(',')
                    .append(scan.getRiskLevel()).append(',')
                    .append(scan.getTotalMethods()).append(',')
                    .append(scan.getDangerousCount()).append(',')
                    .append(scan.getSafeCount()).append(',')
                    .append(scan.getCreatedAt())
                    .append('\n');
        }
        return csv.toString();
    }

    private AdminUserDTO toUserDto(User user) {
        return AdminUserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .scanCount(user.getScans().size())
                .build();
    }

    private boolean matchesUserFilters(AdminUserDTO dto, String search, String role) {
        boolean searchOk = search == null || search.isBlank() || dto.getUsername().toLowerCase().contains(search.toLowerCase());
        boolean roleOk = role == null || role.isBlank() || dto.getRole().equalsIgnoreCase(role);
        return searchOk && roleOk;
    }

    private AdminScanDTO toScanDto(Scan scan) {
        return AdminScanDTO.builder()
                .scanId(scan.getId())
                .username(scan.getUser().getUsername())
                .filename(scan.getFileName())
                .riskLevel(scan.getRiskLevel())
                .totalMethods(scan.getTotalMethods())
                .dangerousCount(scan.getDangerousCount())
                .safeCount(scan.getSafeCount())
                .createdAt(scan.getCreatedAt())
                .build();
    }

    private String sanitize(String value) {
        return value == null ? "" : value.replace(',', '_').replace('\n', ' ');
    }
}
