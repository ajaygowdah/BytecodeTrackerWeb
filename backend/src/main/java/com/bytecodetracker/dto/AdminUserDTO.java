package com.bytecodetracker.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminUserDTO {
    private Long id;
    private String username;
    private String role;
    private LocalDateTime createdAt;
    private long scanCount;
}
