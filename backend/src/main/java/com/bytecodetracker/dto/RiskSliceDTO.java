package com.bytecodetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RiskSliceDTO {
    private String label;
    private long value;
}
