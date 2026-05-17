package com.bytecodetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TimelinePointDTO {
    private String day;
    private long scans;
}
