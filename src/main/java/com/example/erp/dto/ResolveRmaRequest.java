package com.example.erp.dto;

import com.example.erp.entity.RmaResolutionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResolveRmaRequest {

    @NotNull
    private RmaResolutionType resolutionType;

    private String notes;
}
