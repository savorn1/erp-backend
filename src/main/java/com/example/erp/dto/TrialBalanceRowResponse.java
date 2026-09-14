package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// Exactly one of debitBalance/creditBalance is non-zero — whichever side the
// account's net POSTED activity falls on. Only accounts with any posted
// activity are included (see TrialBalanceResponse).
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrialBalanceRowResponse {

    private Long accountId;
    private String accountCode;
    private String accountName;
    private String accountType;
    private BigDecimal debitBalance;
    private BigDecimal creditBalance;
}
