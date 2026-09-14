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
public class BankAccountResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Long accountId;
    private String accountCode;
    private String accountName;
    private String name;
    private String type;
    private String bankName;
    private String accountNumber;
    private String currency;
    private BigDecimal openingBalance;
    private BigDecimal currentBalance;
    private boolean active;
}
