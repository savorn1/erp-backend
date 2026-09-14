package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOutstandingInvoicesResponse {

    // Approved invoices with a balance still owed — sorted by days overdue, descending.
    private List<PurchaseOutstandingInvoiceRowResponse> rows;
    private long invoiceCount;
    private BigDecimal totalOutstanding;
}
