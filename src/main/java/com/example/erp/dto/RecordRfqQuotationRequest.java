package com.example.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

// The prices a single invited supplier quoted back, one per Rfq line product —
// entered by an admin from the supplier's returned quotation (see
// RfqServiceImpl.recordQuotation).
@Data
public class RecordRfqQuotationRequest {

    @NotEmpty
    @Valid
    private List<RfqQuotationLineItem> lines;
}
