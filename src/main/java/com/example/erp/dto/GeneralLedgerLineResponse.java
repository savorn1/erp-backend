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
public class GeneralLedgerLineResponse {

    private Long journalEntryId;
    private String journalNumber;
    private LocalDate entryDate;
    private String description;
    private BigDecimal debit;
    private BigDecimal credit;
    // Running balance after this line, signed by the account's normal side
    // (debit-normal for ASSET/EXPENSE, credit-normal for LIABILITY/EQUITY/REVENUE).
    private BigDecimal runningBalance;
}
