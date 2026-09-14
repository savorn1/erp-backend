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
public class CashFlowAccountRowResponse {

    private Long bankAccountId;
    private String bankAccountName;
    // Balance immediately before dateFrom (openingBalance + every
    // transaction dated strictly before dateFrom).
    private BigDecimal openingBalance;
    // DEPOSIT + TRANSFER_IN within the period.
    private BigDecimal inflow;
    // WITHDRAWAL + TRANSFER_OUT within the period.
    private BigDecimal outflow;
    private BigDecimal netChange;
    private BigDecimal closingBalance;
}
