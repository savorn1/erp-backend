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
public class TrialBalanceResponse {

    private LocalDate asOfDate;
    private List<TrialBalanceRowResponse> rows;
    // Always equal to each other for POSTED entries, since every one was
    // balanced when posted (see JournalEntryServiceImpl) — shown both so a
    // mismatch would actually be visible if it ever happened.
    private BigDecimal debitTotal;
    private BigDecimal creditTotal;
}
