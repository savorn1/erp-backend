package com.example.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateQuotationRequest {

    @NotNull
    private Long companyId;

    // Standalone quotation, not sourced from an opportunity — leave null
    // when created directly rather than via
    // OpportunityController.convertToQuotation.
    private Long customerId;

    @NotNull
    private LocalDate quotationDate;

    private LocalDate validUntil;

    private String notes;

    // Optional reference-only foreign currency — see Quotation's own
    // comment. Both null or both set, enforced in QuotationServiceImpl.
    private String foreignCurrency;
    private BigDecimal exchangeRate;

    @NotEmpty
    @Valid
    private List<QuotationLineRequest> lines;
}
