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
public class GeneralLedgerResponse {

    private Long accountId;
    private String accountCode;
    private String accountName;
    private String accountType;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private BigDecimal openingBalance;
    private List<GeneralLedgerLineResponse> lines;
    private BigDecimal closingBalance;
}
