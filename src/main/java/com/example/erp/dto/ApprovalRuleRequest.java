package com.example.erp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApprovalRuleRequest {

    @NotNull
    private Long companyId;

    // "SALES_ORDER" | "PURCHASE_ORDER" — validated in ApprovalRuleServiceImpl.
    @NotBlank
    private String documentType;

    // Null means "applies regardless of amount."
    private BigDecimal minAmount;

    @Min(1)
    private int requiredApprovals;

    private boolean active = true;
}
