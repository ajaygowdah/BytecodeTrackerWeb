package com.bytecodetracker.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DeleteBulkRequest {
    @NotEmpty
    private List<Long> ids;
}
