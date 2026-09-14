package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankReconciliationResponse {

    private LocalDate statementDate;
    private BigDecimal statementBalance;
    // Sum of every reconciled transaction on this account (opening balance +
    // signed reconciled amounts) as of now, regardless of date — compare
    // against statementBalance to see if the account ties out.
    private BigDecimal reconciledBalance;
    // statementBalance - reconciledBalance. Zero means it ties out.
    private BigDecimal difference;
    private List<BankTransactionResponse> reconciledTransactions;
}
