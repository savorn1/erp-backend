package com.example.erp.dto;

import com.example.erp.entity.PettyCashEntryType;
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
public class PettyCashEntryResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private PettyCashEntryType type;
    private String entryNumber;
    private LocalDate entryDate;
    private Long accountId;
    private String accountLabel;
    private BigDecimal amount;
    private String description;
    private String createdBy;
}
