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
public class PettyCashSummaryResponse {

    private Long companyId;
    private BigDecimal toppedUp;
    private BigDecimal expensed;
    private BigDecimal balance;
}
