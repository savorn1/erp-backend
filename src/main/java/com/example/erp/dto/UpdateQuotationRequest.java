package com.example.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateQuotationRequest {

    private Long customerId;

    @NotNull
    private LocalDate quotationDate;

    private LocalDate validUntil;

    private String notes;

    private String foreignCurrency;
    private BigDecimal exchangeRate;

    @NotEmpty
    @Valid
    private List<QuotationLineRequest> lines;
}
