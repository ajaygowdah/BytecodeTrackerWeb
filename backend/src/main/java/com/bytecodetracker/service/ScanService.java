package com.bytecodetracker.service;

import com.bytecodetracker.dto.*;
import com.bytecodetracker.model.*;
import com.bytecodetracker.repository.ScanRepository;
import com.bytecodetracker.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Service
public class ScanService {

    private final ScanRepository scanRepository;
    private final UserRepository userRepository;
    private final BytecodeAnalyzerService bytecodeAnalyzerService;

    @Value("${app.upload-dir}")
    private String uploadDir;

    public ScanService(ScanRepository scanRepository,
                       UserRepository userRepository,
                       BytecodeAnalyzerService bytecodeAnalyzerService) {
        this.scanRepository = scanRepository;
        this.userRepository = userRepository;
        this.bytecodeAnalyzerService = bytecodeAnalyzerService;
    }

    @Transactional
    public ScanResultDTO uploadAndAnalyze(String username, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "No file was uploaded");
        }

        String originalName = file.getOriginalFilename() == null ? "unknown.class" : file.getOriginalFilename();
        if (!originalName.endsWith(".class")) {
            throw new ResponseStatusException(BAD_REQUEST, "Only .class files are allowed");
        }

        User user = getUser(username);

        try {
            Path uploadPath = Path.of(uploadDir);
            Files.createDirectories(uploadPath);
            Path targetPath = uploadPath.resolve(UUID.randomUUID() + "-" + originalName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            byte[] bytes = Files.readAllBytes(targetPath);
            BytecodeAnalyzerService.AnalysisSummary summary = bytecodeAnalyzerService.analyze(bytes);

            Scan scan = Scan.builder()
                    .user(user)
                    .fileName(originalName)
                    .fileSize(Math.toIntExact(file.getSize()))
                    .totalMethods(summary.getTotalMethods())
                    .dangerousCount(summary.getDangerousCount())
                    .safeCount(summary.getSafeCount())
                    .riskLevel(summary.getRiskLevel())
                    .build();

            ClassAnalysis classAnalysis = ClassAnalysis.builder()
                    .scan(scan)
                    .className(summary.getClassName())
                    .build();
            scan.getClassAnalyses().add(classAnalysis);

            for (BytecodeAnalyzerService.MethodRisk methodRisk : summary.getMethodRisks()) {
                MethodAnalysis method = MethodAnalysis.builder()
                        .classAnalysis(classAnalysis)
                        .methodName(methodRisk.getMethodName())
                        .status(methodRisk.getStatus())
                        .riskLevel(methodRisk.getRiskLevel())
                        .riskReason(methodRisk.getRiskReason())
                        .build();
                classAnalysis.getMethods().add(method);
            }

            for (BytecodeAnalyzerService.ViolationRisk violationRisk : summary.getViolations()) {
                Violation violation = Violation.builder()
                        .scan(scan)
                        .methodName(violationRisk.getMethodName())
                        .riskLevel(violationRisk.getRiskLevel())
                        .reason(violationRisk.getReason())
                        .build();
                scan.getViolations().add(violation);
            }

            Scan saved = scanRepository.save(scan);
            return toResult(saved);
        } catch (IOException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Corrupt .class file or unreadable upload");
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid bytecode format");
        }
    }

    @Transactional
    public PagedResponse<ScanResultDTO> getUserScans(String username, int page, int size) {
        User user = getUser(username);
        Pageable pageable = PageRequest.of(page, size);
        Page<ScanResultDTO> mapped = scanRepository.findByUser(user, pageable).map(this::toResultWithoutGraph);
        return PagedResponse.from(mapped);
    }

    @Transactional
    public ScanResultDTO getScan(String username, Long id) {
        User user = getUser(username);
        Scan scan = scanRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Scan not found"));
        return toResult(scan);
    }

    @Transactional
    public void deleteOwnScan(String username, Long id) {
        User user = getUser(username);
        Scan scan = scanRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Scan not found"));
        scanRepository.delete(scan);
    }

    @Transactional
    public void deleteByIds(List<Long> ids) {
        for (Long id : ids) {
            scanRepository.deleteById(id);
        }
    }

    public ScanResultDTO toResultWithoutGraph(Scan scan) {
        return ScanResultDTO.builder()
                .scanId(scan.getId())
                .username(scan.getUser().getUsername())
                .fileName(scan.getFileName())
                .fileSize(scan.getFileSize())
                .totalMethods(scan.getTotalMethods())
                .dangerousCount(scan.getDangerousCount())
                .safeCount(scan.getSafeCount())
                .riskLevel(scan.getRiskLevel())
                .createdAt(scan.getCreatedAt())
                .classes(List.of())
                .violations(List.of())
                .build();
    }

    public ScanResultDTO toResult(Scan scan) {
        List<ClassAnalysisDTO> classes = scan.getClassAnalyses().stream().map(classAnalysis -> ClassAnalysisDTO.builder()
                .className(classAnalysis.getClassName())
                .methods(classAnalysis.getMethods().stream().map(method -> MethodAnalysisDTO.builder()
                        .methodName(method.getMethodName())
                        .status(method.getStatus())
                        .riskLevel(method.getRiskLevel())
                        .riskReason(method.getRiskReason())
                        .build()).toList())
                .build()).toList();

        List<ViolationDTO> violations = scan.getViolations().stream().map(violation -> ViolationDTO.builder()
                .methodName(violation.getMethodName())
                .riskLevel(violation.getRiskLevel())
                .reason(violation.getReason())
                .build()).toList();

        return ScanResultDTO.builder()
                .scanId(scan.getId())
                .username(scan.getUser().getUsername())
                .fileName(scan.getFileName())
                .fileSize(scan.getFileSize())
                .totalMethods(scan.getTotalMethods())
                .dangerousCount(scan.getDangerousCount())
                .safeCount(scan.getSafeCount())
                .riskLevel(scan.getRiskLevel())
                .createdAt(scan.getCreatedAt())
                .classes(classes)
                .violations(violations)
                .build();
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not found"));
    }
}
