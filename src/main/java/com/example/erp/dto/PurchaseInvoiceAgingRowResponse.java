package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// One supplier's outstanding balance bucketed by how many days past its
// invoices' due dates — the accounts-payable mirror of InvoiceAgingRowResponse.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseInvoiceAgingRowResponse {

    // Null on the report's grand-total row.
    private Long supplierId;
    private String supplierName;
    private BigDecimal current;
    private BigDecimal days1To30;
    private BigDecimal days31To60;
    private BigDecimal days61To90;
    private BigDecimal days90Plus;
    private BigDecimal total;
}
