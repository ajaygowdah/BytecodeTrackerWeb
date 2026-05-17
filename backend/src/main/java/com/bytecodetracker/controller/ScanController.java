package com.bytecodetracker.controller;

import com.bytecodetracker.dto.MessageResponse;
import com.bytecodetracker.dto.PagedResponse;
import com.bytecodetracker.dto.ScanResultDTO;
import com.bytecodetracker.service.ScanService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/scans")
public class ScanController {

    private final ScanService scanService;

    public ScanController(ScanService scanService) {
        this.scanService = scanService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ScanResultDTO> upload(Authentication authentication, @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(scanService.uploadAndAnalyze(authentication.getName(), file));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ScanResultDTO>> list(Authentication authentication,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(scanService.getUserScans(authentication.getName(), page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScanResultDTO> getOne(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(scanService.getScan(authentication.getName(), id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> delete(Authentication authentication, @PathVariable Long id) {
        scanService.deleteOwnScan(authentication.getName(), id);
        return ResponseEntity.ok(new MessageResponse("Scan deleted"));
    }
}
