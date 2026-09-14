package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxReportRateRowResponse {

    // The raw line taxRate percent this row groups (see TaxReportServiceImpl —
    // this isn't linked to a named TaxRate, just the percent value typed on
    // the invoice/purchase-invoice lines).
    private BigDecimal taxRatePercent;
    // Sum of each line's post-discount base the tax was calculated on.
    private BigDecimal taxableAmount;
    private BigDecimal taxAmount;
}
