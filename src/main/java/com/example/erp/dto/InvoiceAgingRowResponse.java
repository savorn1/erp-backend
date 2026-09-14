package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// One customer's outstanding balance bucketed by how many days past its
// invoices' due dates — the standard AR aging report shape. `total` is
// the same figure InvoiceResponse.outstandingAmount would sum to across
// that customer's approved invoices.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceAgingRowResponse {

    // Null on the report's grand-total row.
    private Long customerId;
    private String customerName;
    private BigDecimal current;
    private BigDecimal days1To30;
    private BigDecimal days31To60;
    private BigDecimal days61To90;
    private BigDecimal days90Plus;
    private BigDecimal total;
}
