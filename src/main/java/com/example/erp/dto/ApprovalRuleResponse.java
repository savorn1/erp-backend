package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRuleResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private String documentType;
    private BigDecimal minAmount;
    private int requiredApprovals;
    private boolean active;
}
