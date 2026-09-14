package com.example.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ConvertOpportunityToQuotationRequest {

    @NotNull
    private LocalDate quotationDate;

    private LocalDate validUntil;

    private String notes;

    @NotEmpty
    @Valid
    private List<QuotationLineRequest> lines;
}
