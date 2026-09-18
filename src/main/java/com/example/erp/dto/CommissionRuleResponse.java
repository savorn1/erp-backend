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
public class CommissionRuleResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Long userId;
    private String userName;
    private BigDecimal ratePercent;
    private boolean active;
}
