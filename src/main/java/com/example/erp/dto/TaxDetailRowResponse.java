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
public class TaxDetailRowResponse {

    // "Output" (sales invoice) or "Input" (purchase invoice).
    private String type;
    private LocalDate date;
    private String reference;
    private Long partyId;
    private String partyName;
    private BigDecimal taxableAmount;
    private BigDecimal taxRatePercent;
    private BigDecimal taxAmount;
}
