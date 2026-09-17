package com.example.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// Used for both "generate from sales order" and "generate from delivery" —
// lines are copied from the source, not entered manually (see
// InvoiceServiceImpl.createFromSalesOrder/createFromDelivery).
@Data
public class CreateInvoiceRequest {

    @NotNull
    private LocalDate invoiceDate;

    private LocalDate dueDate;

    private String notes;

    // Optional reference-only foreign currency — see Invoice's own comment.
    // Both null or both set, enforced in InvoiceServiceImpl.
    private String foreignCurrency;
    private BigDecimal exchangeRate;
}
