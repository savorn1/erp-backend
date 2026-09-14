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
public class SalesOutstandingInvoicesResponse {

    // Approved invoices with a balance still owed — sorted by days overdue, descending.
    private List<SalesOutstandingInvoiceRowResponse> rows;
    private long invoiceCount;
    private BigDecimal totalOutstanding;
}
