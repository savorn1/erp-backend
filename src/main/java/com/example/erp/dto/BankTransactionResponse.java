package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankTransactionResponse {

    private Long id;
    private Long companyId;
    private Long bankAccountId;
    private String bankAccountName;
    private String transactionNumber;
    private LocalDate transactionDate;
    private String type;
    private BigDecimal amount;
    private String reference;
    private String description;
    private Long relatedTransactionId;
    private String relatedTransactionNumber;
    private String relatedBankAccountName;
    private boolean reconciled;
    private LocalDate reconciledDate;
    private String createdBy;
}
