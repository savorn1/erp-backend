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
public class QuotationResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    // Legacy — see Quotation.opportunityId's comment. leadId is the live linkage.
    private Long opportunityId;
    private String opportunityName;
    private Long leadId;
    private String leadName;
    private Long customerId;
    private String customerName;
    private String quotationNumber;
    private LocalDate quotationDate;
    private LocalDate validUntil;
    private String status;
    private String notes;
    private String createdBy;
    private BigDecimal totalAmount;
    private String foreignCurrency;
    private BigDecimal exchangeRate;
    // totalAmount / exchangeRate, only when both foreignCurrency and
    // exchangeRate are set — a display-only convenience, never stored.
    private BigDecimal foreignTotalAmount;
    private List<QuotationLineResponse> lines;
}
