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
public class CollectionBySalespersonRowResponse {

    // The username that created the underlying sales order (or the invoice,
    // for invoices raised without an order) — same attribution as the
    // "Sales by salesperson" report. "Unallocated" for payment amounts not
    // yet applied to any invoice.
    private String salesperson;
    private long invoiceCount;
    private BigDecimal allocatedAmount;
}
