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
public class TransferRowResponse {

    // The TRANSFER_OUT leg; the paired TRANSFER_IN is resolved via relatedTransactionId.
    private Long transactionId;
    private String transactionNumber;
    private LocalDate transactionDate;
    private Long fromAccountId;
    private String fromAccountName;
    private Long toAccountId;
    private String toAccountName;
    private BigDecimal amount;
    private String reference;
    private String description;
}
