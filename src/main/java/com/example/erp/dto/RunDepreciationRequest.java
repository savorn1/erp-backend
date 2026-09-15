package com.example.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RunDepreciationRequest {

    @NotNull
    private Long companyId;

    @NotNull
    private Long accountingPeriodId;
}
