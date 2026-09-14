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
public class PaymentReconciliationRowResponse {

    private Long bankAccountId;
    private String bankAccountName;
    private String accountType;
    // DEPOSIT + TRANSFER_IN.
    private BigDecimal deposits;
    // WITHDRAWAL + TRANSFER_OUT.
    private BigDecimal withdrawals;
    private long reconciledCount;
    private BigDecimal reconciledAmount;
    private long unreconciledCount;
    private BigDecimal unreconciledAmount;
}
