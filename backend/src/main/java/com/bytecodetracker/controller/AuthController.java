package com.bytecodetracker.controller;

import com.bytecodetracker.dto.*;
import com.bytecodetracker.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(new MessageResponse("User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/claims")
    public ResponseEntity<Object> claims(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(new MessageResponse("Missing Authorization header"));
        }
        String token = authHeader.substring(7);
        try {
            String username = authService.getJwtUtil().getUsernameFromToken(token);
            String role = authService.getJwtUtil().getRoleFromToken(token);
            boolean valid = authService.getJwtUtil().isTokenValid(token);
            return ResponseEntity.ok().body(new Object() {
                public final String user = username;
                public final String roleName = role;
                public final boolean validToken = valid;
            });
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid token: " + e.getMessage()));
        }
    }
}
