package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private String journalNumber;
    private LocalDate entryDate;
    private String description;
    private String status;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private Long reversalOfJournalEntryId;
    private String reversalOfJournalNumber;
    private Long reversedByJournalEntryId;
    private String reversedByJournalNumber;
    private String createdBy;
    private LocalDateTime createdAt;
    private String postedBy;
    private LocalDateTime postedAt;
    private List<JournalEntryLineResponse> lines;
}
